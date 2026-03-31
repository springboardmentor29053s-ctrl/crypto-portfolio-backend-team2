package com.example.backend.service;

import com.example.backend.dto.TradeRequestDTO;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;
    private final HoldingsRepository holdingsRepository;
    private final UserRepository userRepository;
    private final ExchangeRepository exchangeRepository;

    public TradeService(
            TradeRepository tradeRepository,
            HoldingsRepository holdingsRepository,
            UserRepository userRepository,
            ExchangeRepository exchangeRepository) {

        this.tradeRepository = tradeRepository;
        this.holdingsRepository = holdingsRepository;
        this.userRepository = userRepository;
        this.exchangeRepository = exchangeRepository;
    }

    public Trade addTrade(TradeRequestDTO dto) {

        System.out.println("Executing Trade -> " + dto.getSide() + " " + dto.getQuantity() + " " + dto.getAssetSymbol());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exchange exchange = exchangeRepository.findById(dto.getExchangeId())
                .orElseThrow(() -> new RuntimeException("Exchange not found"));

        double walletBalance = user.getWalletBalance() == null ? 0.0 : user.getWalletBalance();

        List<Holding> existingHoldings =
                holdingsRepository.findByUserAndExchangeAndSymbol(
                        user,
                        exchange,
                        dto.getAssetSymbol().toUpperCase()
                );

        Holding holding;

        if (existingHoldings != null && !existingHoldings.isEmpty()) {
            holding = existingHoldings.get(0);

            if (existingHoldings.size() > 1) {
                for (int i = 1; i < existingHoldings.size(); i++) {
                    holdingsRepository.delete(existingHoldings.get(i));
                }
            }

        } else {
            holding = new Holding();
            holding.setUser(user);
            holding.setExchange(exchange);
            holding.setSymbol(dto.getAssetSymbol().toUpperCase());
            holding.setAvailable(0.0);
            holding.setLocked(0.0);
            holding.setAvgCost(0.0);
        }

        double currentQty = holding.getAvailable() == null ? 0.0 : holding.getAvailable();
        double currentAvg = holding.getAvgCost() == null ? 0.0 : holding.getAvgCost();

        Trade trade = new Trade();
        trade.setUserId(dto.getUserId());
        trade.setAssetSymbol(dto.getAssetSymbol().toUpperCase());
        trade.setSide(dto.getSide());
        trade.setQuantity(dto.getQuantity());
        trade.setPrice(dto.getPrice());
        trade.setFee(dto.getFee());
        trade.setExchangeId(dto.getExchangeId());
        trade.setExecutedAt(LocalDateTime.now());

        if (dto.getSide().equalsIgnoreCase("BUY")) {

            double totalAmount = dto.getQuantity() * dto.getPrice() + dto.getFee();

            if (walletBalance < totalAmount) {
                throw new RuntimeException("Insufficient wallet balance");
            }

            user.setWalletBalance(walletBalance - totalAmount);
            userRepository.save(user);

            double totalCost = (currentQty * currentAvg) + (dto.getQuantity() * dto.getPrice());
            double totalQuantity = currentQty + dto.getQuantity();

            holding.setAvailable(totalQuantity);

            if (totalQuantity > 0) {
                holding.setAvgCost(totalCost / totalQuantity);
            }

            trade.setRealizedPnl(0.0);

        } else if (dto.getSide().equalsIgnoreCase("SELL")) {

            if (dto.getQuantity() > currentQty) {
                throw new RuntimeException("Not enough holdings to sell");
            }

            double sellAmount = dto.getQuantity() * dto.getPrice() - dto.getFee();
            user.setWalletBalance(walletBalance + sellAmount);
            userRepository.save(user);

            double realized = ((dto.getPrice() - currentAvg) * dto.getQuantity()) - dto.getFee();
            trade.setRealizedPnl(realized);

            holding.setAvailable(currentQty - dto.getQuantity());
        }

        holdingsRepository.save(holding);
        return tradeRepository.save(trade);
    }

    public Double getTotalRealizedPnl(Long userId) {
        return tradeRepository.findByUserId(userId)
                .stream()
                .mapToDouble(t -> t.getRealizedPnl() == null ? 0.0 : t.getRealizedPnl())
                .sum();
    }

    public List<Trade> getTradesByUser(Long userId) {
        return tradeRepository.findByUserId(userId);
    }

    public Map<String, Object> getTaxReport(Long userId) {
        List<Trade> trades = tradeRepository.findByUserId(userId);

        trades.forEach(t -> {
            t.setPrice(Math.round((t.getPrice() == null ? 0.0 : t.getPrice()) * 100.0) / 100.0);
            t.setFee(Math.round((t.getFee() == null ? 0.0 : t.getFee()) * 100.0) / 100.0);
            t.setRealizedPnl(Math.round((t.getRealizedPnl() == null ? 0.0 : t.getRealizedPnl()) * 100.0) / 100.0);
        });

        double totalRealizedPnl = trades.stream()
                .mapToDouble(t -> t.getRealizedPnl() == null ? 0.0 : t.getRealizedPnl())
                .sum();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("trades", trades);
        response.put("totalRealizedPnl", Math.round(totalRealizedPnl * 100.0) / 100.0);

        return response;
    }
}