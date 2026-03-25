package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.AssetValueDto;
import com.crypto_portfolio_tracker.dto.HoldingDTO;
import com.crypto_portfolio_tracker.dto.PortfolioSummaryResponse;
import com.crypto_portfolio_tracker.dto.PortfolioValueResponse;
import com.crypto_portfolio_tracker.model.Holding;
import com.crypto_portfolio_tracker.model.PortfolioHistory;
import com.crypto_portfolio_tracker.model.TradeRecord;
import com.crypto_portfolio_tracker.model.UserEntity;
import com.crypto_portfolio_tracker.repository.HoldingRepository;
import com.crypto_portfolio_tracker.repository.PortfolioHistoryRepository;
import com.crypto_portfolio_tracker.repository.TradeRecordRepository;
import com.crypto_portfolio_tracker.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class PortfolioService {

     private final TradeRecordRepository tradeRecordRepo;
     private final HoldingRepository holdingRepo;
     private final UserRepository userRepo;
     private final PortfolioHistoryRepository portfolioHistoryRepo;

    public PortfolioService(TradeRecordRepository tradeRecordRepo, HoldingRepository holdingRepo, UserRepository userRepo, PortfolioHistoryRepository portfolioHistoryRepo) {
        this.tradeRecordRepo = tradeRecordRepo;
        this.holdingRepo = holdingRepo;
        this.userRepo = userRepo;
        this.portfolioHistoryRepo = portfolioHistoryRepo;
    }

    public  void recalculate(String email){
         //  Fetch real user from DB
         UserEntity user = userRepo.findByEmail(email)
                 .orElseThrow(() -> new RuntimeException("User not found"));

        // 1 Clear existing holdings
        holdingRepo.deleteAll(holdingRepo.findByUser(user));

        // 2 Get trades ordered by time
        List<TradeRecord> trades = tradeRecordRepo.findByUser(user)
                .stream()
                .sorted(Comparator.comparing(TradeRecord::getTradeTime))
                .toList();

        // 3 Replay trades one by one
        for (TradeRecord trade : trades) {
            updateHolding(user, trade);
        }
     }

    @Transactional
    public void updateHolding(UserEntity user, TradeRecord trade) {

        String asset = extractBaseAsset(trade.getSymbol());

        // 1️ Initialize holding safely
        Holding holding = holdingRepo
                .findByUserAndAsset(user, asset)
                .orElseGet(() -> Holding.builder()
                        .user(user)
                        .asset(asset)
                        .quantity(BigDecimal.ZERO)
                        .averageCost(BigDecimal.ZERO)
                        .realizedProfitLoss(BigDecimal.ZERO)
                        .build()
                );

        if (holding.getRealizedProfitLoss() == null) {
            holding.setRealizedProfitLoss(BigDecimal.ZERO);
        }

        BigDecimal tradeQty = trade.getQuantity() != null ? trade.getQuantity() : BigDecimal.ZERO;
        BigDecimal tradePrice = trade.getPrice() != null ? trade.getPrice() : BigDecimal.ZERO;
        BigDecimal commission = trade.getCommission() != null ? trade.getCommission() : BigDecimal.ZERO;

        // 2️ Adjusting quantity if commission paid in base asset
        if (asset.equalsIgnoreCase(trade.getCommissionAsset())) {
            tradeQty = tradeQty.subtract(commission);
            if (tradeQty.compareTo(BigDecimal.ZERO) < 0) {
                tradeQty = BigDecimal.ZERO;
            }
        }

        if ("BUY".equalsIgnoreCase(trade.getSide())) {
            BigDecimal oldQty = holding.getQuantity();
            BigDecimal oldAvg = holding.getAverageCost();

            BigDecimal newQty = oldQty.add(tradeQty);
            BigDecimal totalCost = oldQty.multiply(oldAvg).add(tradeQty.multiply(tradePrice));

            BigDecimal newAvg = newQty.compareTo(BigDecimal.ZERO) > 0
                    ? totalCost.divide(newQty, 8, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            holding.setQuantity(newQty);
            holding.setAverageCost(newAvg);

        }else if ("SELL".equalsIgnoreCase(trade.getSide())) {

            BigDecimal oldQty = holding.getQuantity();
            BigDecimal avgCost = holding.getAverageCost();

            if (tradeQty.compareTo(oldQty) > 0) {
               throw new RuntimeException("Insufficient asset Balance");// prevent crash
            }
            //sell logic
            BigDecimal grossProfit = tradePrice.subtract(avgCost)
                    .multiply(tradeQty);

            BigDecimal netProfit = grossProfit;

            if (!asset.equalsIgnoreCase(trade.getCommissionAsset())) {
                netProfit = netProfit.subtract(commission);
            }

            if (trade.getCommissionAsset().equalsIgnoreCase(asset)) {
                tradeQty = tradeQty.subtract(commission);
            }
            else if (trade.getCommissionAsset().equalsIgnoreCase("USDT")) {
                netProfit = netProfit.subtract(commission);
            }

            holding.setRealizedProfitLoss(holding.getRealizedProfitLoss().add(netProfit));

            BigDecimal newQty = oldQty.subtract(tradeQty);
            holding.setQuantity(newQty);

            if (newQty.compareTo(BigDecimal.ZERO) == 0) {
                holding.setAverageCost(BigDecimal.ZERO);
            }
        }
        else {
            throw new RuntimeException("Unknown trade side: " + trade.getSide());
        }

        holdingRepo.saveAndFlush(holding);
    }

    private String extractBaseAsset(String symbol) {
        if (symbol.endsWith("USDT")) {
            return symbol.replace("USDT", "");
        }
        throw new RuntimeException("Unsupported symbol format: " + symbol);
    }

    public List<HoldingDTO> getHoldingsForUser(UserEntity user) {
        List<Holding> holdings = holdingRepo.findByUser(user);

        return holdings.stream()
                .map(h -> new HoldingDTO(
                        h.getId(),
                        h.getAsset(),
                        h.getQuantity(),
                        h.getAverageCost(),
                        h.getRealizedProfitLoss()
                ))
                .toList();
    }

    public List<Holding> getUserPortfolio(UserEntity user) {
        return holdingRepo.findByUser(user);
    }

    private BigDecimal getMarketPrice(String symbol) {

        String url = "https://testnet.binance.vision/api/v3/ticker/price?symbol=" + symbol;
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> response = restTemplate.getForObject(url, Map.class);
        return new BigDecimal(response.get("price"));
    }

    public PortfolioValueResponse getPortfolioValue(UserEntity user){

        List<Holding> holdings = holdingRepo.findByUser(user);

        BigDecimal totalValue = BigDecimal.ZERO;

        List<AssetValueDto> assets = new ArrayList<>();

        for(Holding h : holdings){

            if(h.getQuantity().compareTo(BigDecimal.ZERO) == 0){
                continue;
            }

            String symbol = h.getAsset();
            String pair = symbol + "USDT";

            BigDecimal marketPrice = getMarketPrice(pair);

            BigDecimal value = marketPrice.multiply(h.getQuantity());

            AssetValueDto dto = new AssetValueDto();
            dto.setSymbol(symbol);
            dto.setQuantity(h.getQuantity());
            dto.setMarketPrice(marketPrice);
            dto.setValue(value);

            assets.add(dto);

            totalValue = totalValue.add(value);
        }

        PortfolioValueResponse response = new PortfolioValueResponse();
        response.setTotalPortfolioValue(totalValue);
        response.setAssets(assets);

        return response;
    }


    @Scheduled(fixedRate = 3600000)
    public void recordPortfolioValue(){

        List<UserEntity> users = userRepo.findAll();

        for(UserEntity user : users){

            BigDecimal value = getPortfolioValue(user).getTotalPortfolioValue();

            PortfolioHistory history = new PortfolioHistory();

            history.setUser(user);
            history.setPortfolioValue(value);
            history.setTimestamp(LocalDateTime.now());

            portfolioHistoryRepo.save(history);
        }
    }

    //realized and unrealizepnl with total value
    public PortfolioSummaryResponse getPortfolioSummary(UserEntity user) {

        List<Holding> holdings = holdingRepo.findByUser(user);

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalRealized = BigDecimal.ZERO;
        BigDecimal totalUnrealized = BigDecimal.ZERO;

        List<AssetValueDto> assets = new ArrayList<>();

        for (Holding h : holdings) {

            if (h.getQuantity().compareTo(BigDecimal.ZERO) == 0) continue;

            String symbol = h.getAsset();

            if ("USDT".equalsIgnoreCase(symbol)) continue;

            String pair = symbol + "USDT";

            BigDecimal marketPrice = getMarketPrice(pair);

            // VALUE
            BigDecimal value = marketPrice.multiply(h.getQuantity());
            totalValue = totalValue.add(value);

            // REALIZED
            BigDecimal realized = h.getRealizedProfitLoss() != null
                    ? h.getRealizedProfitLoss()
                    : BigDecimal.ZERO;

            totalRealized = totalRealized.add(realized);

            // UNREALIZED
            BigDecimal unrealized = marketPrice
                    .subtract(h.getAverageCost())
                    .multiply(h.getQuantity());

            totalUnrealized = totalUnrealized.add(unrealized);

            // DTO
            AssetValueDto dto = new AssetValueDto();
            dto.setSymbol(symbol);
            dto.setQuantity(h.getQuantity());
            dto.setMarketPrice(marketPrice);
            dto.setValue(value);

            assets.add(dto);
        }

        PortfolioSummaryResponse res = new PortfolioSummaryResponse();

        res.setTotalPortfolioValue(totalValue);
        res.setRealizedPnl(totalRealized);
        res.setUnrealizedPnl(totalUnrealized);
        res.setTotalPnl(totalRealized.add(totalUnrealized));
        res.setAssets(assets);

        return res;
    }
}
