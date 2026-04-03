package com.example.backend.service;

import com.example.backend.dto.TradeRequestDTO;
import com.example.backend.entity.Exchange;
import com.example.backend.entity.Holding;
import com.example.backend.entity.Trade;
import com.example.backend.entity.User;
import com.example.backend.repository.ExchangeRepository;
import com.example.backend.repository.HoldingsRepository;
import com.example.backend.repository.TradeRepository;
import com.example.backend.repository.UserRepository;
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

    public TradeService(TradeRepository tradeRepository,
                        HoldingsRepository holdingsRepository,
                        UserRepository userRepository,
                        ExchangeRepository exchangeRepository) {
        this.tradeRepository = tradeRepository;
        this.holdingsRepository = holdingsRepository;
        this.userRepository = userRepository;
        this.exchangeRepository = exchangeRepository;
    }

    public Trade addTrade(TradeRequestDTO dto) {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exchange exchange = exchangeRepository.findById(dto.getExchangeId())
                .orElseThrow(() -> new RuntimeException("Exchange not found"));

        double walletBalance = user.getWalletBalance() == null ? 0.0 : user.getWalletBalance();

        List<Holding> existingHoldings = holdingsRepository.findByUserAndExchangeAndSymbol(
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
        trade.setSide(dto.getSide().toUpperCase());
        trade.setQuantity(dto.getQuantity());
        trade.setPrice(dto.getPrice());
        trade.setFee(dto.getFee() == null ? 0.0 : dto.getFee());
        trade.setExchangeId(dto.getExchangeId());
        trade.setExecutedAt(dto.getExecutedAt() != null ? dto.getExecutedAt() : LocalDateTime.now());

        if ("BUY".equalsIgnoreCase(dto.getSide())) {

            double totalAmount = dto.getQuantity() * dto.getPrice() + trade.getFee();

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

        } else if ("SELL".equalsIgnoreCase(dto.getSide())) {

            if (dto.getQuantity() > currentQty) {
                throw new RuntimeException("Not enough holdings to sell");
            }

            double sellAmount = dto.getQuantity() * dto.getPrice() - trade.getFee();
            user.setWalletBalance(walletBalance + sellAmount);
            userRepository.save(user);

            double realized = ((dto.getPrice() - currentAvg) * dto.getQuantity()) - trade.getFee();
            trade.setRealizedPnl(realized);

            holding.setAvailable(currentQty - dto.getQuantity());
        } else {
            throw new RuntimeException("Invalid trade side");
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
        List<Trade> allTrades = tradeRepository.findByUserId(userId);

        List<Trade> trades = allTrades.stream()
                .filter(t -> "SELL".equalsIgnoreCase(t.getSide()))
                .toList();

        double totalProfit = 0.0;
        double totalLoss = 0.0;

        for (Trade t : trades) {
            double pnl = t.getRealizedPnl() == null ? 0.0 : t.getRealizedPnl();

            if (pnl >= 0) {
                totalProfit += pnl;
            } else {
                totalLoss += pnl;
            }
        }

        double netPnl = totalProfit + totalLoss;

        totalProfit = Math.round(totalProfit * 100.0) / 100.0;
        totalLoss = Math.round(totalLoss * 100.0) / 100.0;
        netPnl = Math.round(netPnl * 100.0) / 100.0;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("trades", trades);
        response.put("totalProfit", totalProfit);
        response.put("totalLoss", totalLoss);
        response.put("netPnl", netPnl);

        return response;
    }
}