package demo.Crypto_Portfolio.app.service;

// DTOs
import demo.Crypto_Portfolio.app.dto.portfolio.HoldingLiveDTO;
import demo.Crypto_Portfolio.app.dto.portfolio.PortfolioSummaryDTO;
import demo.Crypto_Portfolio.app.dto.portfolio.RiskAlertDTO;
import demo.Crypto_Portfolio.app.dto.portfolio.PnlSummaryDTO;

// Models
import demo.Crypto_Portfolio.app.model.Holding;
import demo.Crypto_Portfolio.app.model.PriceSnapshot;
import demo.Crypto_Portfolio.app.model.Trade;

// Repositories
import demo.Crypto_Portfolio.app.repository.HoldingRepository;
import demo.Crypto_Portfolio.app.repository.PriceSnapshotRepository;
import demo.Crypto_Portfolio.app.repository.TradeRepository;
import demo.Crypto_Portfolio.app.repository.RiskAlertRepository;

// Services
import demo.Crypto_Portfolio.app.service.CryptoService;
import demo.Crypto_Portfolio.app.service.ScamService;

// Spring
import org.springframework.stereotype.Service;

// Java Utils
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


@Service
public class PortfolioService {

    private final HoldingRepository holdingRepository;
    private final TradeRepository tradeRepository;
    private final PriceSnapshotRepository snapshotRepository;
    private final CryptoService cryptoService;
    private final ScamService scamService;
    private final RiskAlertRepository riskAlertRepository;

    public PortfolioService(
            HoldingRepository holdingRepository,
            TradeRepository tradeRepository,
            PriceSnapshotRepository snapshotRepository,
            CryptoService cryptoService,
            ScamService scamService,
            RiskAlertRepository riskAlertRepository) {

        this.holdingRepository = holdingRepository;
        this.tradeRepository = tradeRepository;
        this.snapshotRepository = snapshotRepository;
        this.cryptoService = cryptoService;
        this.scamService = scamService;
        this.riskAlertRepository = riskAlertRepository;
    }

    // =============================
    // HOLDINGS
    // =============================

    public List<Holding> getHoldings(Long userId) {
        return holdingRepository.findByUserId(userId);
    }

    // =============================
    // HOLDINGS WITH LIVE DATA
    // =============================

    public List<HoldingLiveDTO> getHoldingsWithLive(Long userId) {

        List<Holding> holdings = holdingRepository.findByUserId(userId);

        return holdings.stream().map(h -> {

            double livePrice = snapshotRepository
                    .findTopByCoinSymbolOrderByTimestampDesc(h.getAssetSymbol())
                    .map(PriceSnapshot::getPrice)
                    .orElse(0.0);

            double quantity = h.getQuantity();
            double currentValue = quantity * livePrice;
            double invested = quantity * h.getAvgCost();
            double pnl = currentValue - invested;

            String riskLevel = calculateHoldingRisk(pnl, invested);

            // Scam detection
            boolean isScam = false;
            String scamReason = null;

            if (h.getContractAddress() != null && !h.getContractAddress().isEmpty()) {
                isScam = scamService.isScamToken(h.getContractAddress());
                if (isScam) {
                    scamReason = "Token flagged as scam";
                }
            }

            String riskReason =
                    pnl < 0 ? "Negative returns" :
                            pnl == 0 ? "No profit no loss" :
                                    "Profitable asset";

            return new HoldingLiveDTO(
                    h.getAssetSymbol(),
                    quantity,
                    h.getAvgCost(),
                    livePrice,
                    currentValue,
                    pnl,
                    riskLevel,
                    isScam,
                    scamReason,
                    riskReason
            );

        }).toList();
    }

    // =============================
    // PORTFOLIO SUMMARY
    // =============================

    public PortfolioSummaryDTO getPortfolioSummary(Long userId) {

        double realized = calculateRealizedPnl(userId);
        double unrealized = calculateUnrealizedPnl(userId);

        List<Holding> holdings = holdingRepository.findByUserId(userId);

        double totalInvestment = 0;
        double totalValue = 0;

        for (Holding h : holdings) {

            double livePrice = snapshotRepository
                    .findTopByCoinSymbolOrderByTimestampDesc(h.getAssetSymbol())
                    .map(PriceSnapshot::getPrice)
                    .orElse(0.0);

            totalInvestment += h.getQuantity() * h.getAvgCost();
            totalValue += h.getQuantity() * livePrice;
        }

        PortfolioSummaryDTO dto = new PortfolioSummaryDTO();
        dto.setTotalInvestment(totalInvestment);
        dto.setTotalValue(totalValue);
        dto.setTotalPnl(realized + unrealized);

        return dto;
    }

    // =============================
    // TRADES
    // =============================

    public List<Trade> getTrades(Long userId) {
        return tradeRepository.findByUserId(userId);
    }

    public Trade saveTrade(Long userId, Trade trade) {

        trade.setUserId(userId);
        Trade saved = tradeRepository.save(trade);

        updateHoldingFromTrade(saved);

        return saved;
    }

    // =============================
    // UPDATE HOLDING
    // =============================

    private void updateHoldingFromTrade(Trade trade) {

        String symbol = trade.getSymbol();

        Holding holding = holdingRepository
                .findByUserIdAndAssetSymbol(trade.getUserId(), symbol)
                .orElse(new Holding());

        holding.setUserId(trade.getUserId());
        holding.setAssetSymbol(symbol);

        // Safe contract address
        if (trade.getContractAddress() != null) {
            holding.setContractAddress(trade.getContractAddress());
        }

        double currentQty = holding.getQuantity();

        if ("BUY".equalsIgnoreCase(trade.getSide())) {

            double newQty = currentQty + trade.getQuantity();

            double currentInvestment = currentQty * holding.getAvgCost();
            double newInvestment = trade.getQuantity() * trade.getPrice();

            double avgCost = (currentInvestment + newInvestment) / newQty;

            holding.setQuantity(newQty);
            holding.setAvgCost(avgCost);

        } else if ("SELL".equalsIgnoreCase(trade.getSide())) {

            double newQty = currentQty - trade.getQuantity();

            if (newQty <= 0) {
                holdingRepository.delete(holding);
                return;
            }

            holding.setQuantity(newQty);
        }

        holdingRepository.save(holding);
    }

    // =============================
    // RISK ALERTS
    // =============================

    public List<RiskAlertDTO> getRiskAlerts(Long userId) {

        List<Holding> holdings = holdingRepository.findByUserId(userId);
        List<RiskAlertDTO> alerts = new ArrayList<>();

        double totalValue = holdings.stream()
                .mapToDouble(h -> h.getQuantity() * h.getAvgCost())
                .sum();

        for (Holding h : holdings) {

            String symbol = h.getAssetSymbol();

            double value = h.getQuantity() * h.getAvgCost();
            double exposure = totalValue == 0 ? 0 : value / totalValue;

            if (exposure > 0.5) {
                alerts.add(new RiskAlertDTO(symbol,
                        "⚠️ High concentration in " + symbol,
                        "HIGH"));
            }

            double pnlPercent = ((value - (h.getQuantity() * h.getAvgCost())) / (h.getAvgCost() == 0 ? 1 : h.getAvgCost())) * 100;

            if (pnlPercent < -5) {
                alerts.add(new RiskAlertDTO(symbol,
                        "📉 " + symbol + " dropped significantly",
                        "MEDIUM"));
            }

            // Scam alert (no DB spam)
            if (h.getContractAddress() != null && !h.getContractAddress().isEmpty()) {

                boolean scam = scamService.isScamToken(h.getContractAddress());

                if (scam) {
                    alerts.add(new RiskAlertDTO(symbol,
                            "🚨 Scam token detected",
                            "CRITICAL"));
                }
            }
        }

        return alerts;
    }

    // =============================
    // PnL METHODS
    // =============================

    public double calculateRealizedPnl(Long userId) {

        List<Trade> trades = tradeRepository.findByUserId(userId);

        double realized = 0;

        for (Trade t : trades) {

            if ("SELL".equalsIgnoreCase(t.getSide())) {

                double buyPrice = tradeRepository
                        .findTopByUserIdAndSymbolAndSideOrderByExecutedAtDesc(
                                userId, t.getSymbol(), "BUY")
                        .map(Trade::getPrice)
                        .orElse(0.0);

                realized += (t.getPrice() - buyPrice) * t.getQuantity();
            }
        }

        return realized;
    }

    public double calculateUnrealizedPnl(Long userId) {
        return getHoldingsWithLive(userId).stream()
                .mapToDouble(HoldingLiveDTO::getPnl)
                .sum();
    }

    public PnlSummaryDTO getPnlSummary(Long userId) {

        double realized = calculateRealizedPnl(userId);
        double unrealized = calculateUnrealizedPnl(userId);

        PnlSummaryDTO dto = new PnlSummaryDTO();
        dto.setRealizedPnl(realized);
        dto.setUnrealizedPnl(unrealized);
        dto.setTotalPnl(realized + unrealized);

        return dto;
    }

    // =============================
    // CSV EXPORT
    // =============================

    public String generateCsv(Long userId) {

        List<HoldingLiveDTO> holdings = getHoldingsWithLive(userId);

        StringBuilder csv = new StringBuilder();
        csv.append("Asset,Quantity,Average Cost,Live Price,CurrentValue,Profit & Loss\n");

        for (HoldingLiveDTO h : holdings) {
            csv.append(h.getAssetSymbol()).append(",")
                    .append(h.getQuantity()).append(",")
                    .append(h.getAvgCost()).append(",")
                    .append(h.getLivePrice()).append(",")
                    .append(h.getCurrentValue()).append(",")
                    .append(h.getPnl()).append("\n");
        }

        return csv.toString();
    }

    // =============================
    // HELPER
    // =============================

    private String calculateHoldingRisk(double pnl, double investment) {

        if (investment == 0) return "LOW";

        double percent = (pnl / investment) * 100;

        if (percent <= -5) return "HIGH";
        else if (percent < 0) return "MEDIUM";
        else return "LOW";
    }
    // risk calculation
    public String calculateRiskLevel(Long userId) {

        List<Holding> holdings = holdingRepository.findByUserId(userId);

        double total = holdings.stream()
                .mapToDouble(h -> h.getQuantity() * h.getAvgCost())
                .sum();

        if (total == 0) return "No Holdings";

        for (Holding h : holdings) {

            double value = h.getQuantity() * h.getAvgCost();
            double exposure = value / total;

            if (exposure > 0.7) {
                return "HIGH RISK - Overexposed to " + h.getAssetSymbol();
            }
        }

        return "Balanced Portfolio";
    }
    // =============================
    // TAX CSV EXPORT (NEW)
    // =============================
    public String generateTaxReportCsv(Long userId) {

        List<Trade> trades = tradeRepository.findByUserId(userId);

        StringBuilder csv = new StringBuilder();

        csv.append("Date,Symbol,Type,Quantity,Buy Price,Sell Price,Profit/Loss,Category\n");

        for (Trade t : trades) {

            if ("SELL".equalsIgnoreCase(t.getSide())) {

                double buyPrice = tradeRepository
                        .findTopByUserIdAndSymbolAndSideOrderByExecutedAtDesc(
                                userId, t.getSymbol(), "BUY")
                        .map(Trade::getPrice)
                        .orElse(0.0);

                double profit = (t.getPrice() - buyPrice) * t.getQuantity();

                String category;
                if (profit > 0) {
                    category = "PROFIT";
                } else if (profit < 0) {
                    category = "LOSS";
                } else {
                    category = "NO_GAIN";
                }

                csv.append(t.getExecutedAt()).append(",")
                        .append(t.getSymbol()).append(",")
                        .append(t.getSide()).append(",")
                        .append(t.getQuantity()).append(",")
                        .append(buyPrice).append(",")
                        .append(t.getPrice()).append(",")
                        .append(profit).append(",")
                        .append(category).append("\n");
            }
        }

        return csv.toString();
    }
}