package demo.Crypto_Portfolio.app.dto.portfolio;

public class HoldingLiveDTO {

    private String assetSymbol;
    private double quantity;
    private double avgCost;
    private double livePrice;
    private double currentValue;
    private double unrealizedPnl;
    private String riskLevel;

    // 🔥 NEW FIELDS
    private boolean isScam;
    private String scamReason;
    private String riskReason;

    public HoldingLiveDTO(String assetSymbol,
                          double quantity,
                          double avgCost,
                          double livePrice,
                          double currentValue,
                          double unrealizedPnl,
                          String riskLevel,
                          boolean isScam,
                          String scamReason,
                          String riskReason) {

        this.assetSymbol = assetSymbol;
        this.quantity = quantity;
        this.avgCost = avgCost;
        this.livePrice = livePrice;
        this.currentValue = currentValue;
        this.unrealizedPnl = unrealizedPnl;
        this.riskLevel = riskLevel;

        this.isScam = isScam;
        this.scamReason = scamReason;
        this.riskReason = riskReason;
    }

    public String getAssetSymbol() { return assetSymbol; }
    public double getQuantity() { return quantity; }
    public double getAvgCost() { return avgCost; }
    public double getLivePrice() { return livePrice; }
    public double getCurrentValue() { return currentValue; }
    public double getUnrealizedPnl() { return unrealizedPnl; }
    public String getRiskLevel() { return riskLevel; }

    public boolean isScam() { return isScam; }
    public String getScamReason() { return scamReason; }
    public String getRiskReason() { return riskReason; }

    // Optional helper (used in PnL calc)
    public double getPnl() {
        return unrealizedPnl;
    }
}