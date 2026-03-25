package demo.Crypto_Portfolio.app.dto.portfolio;

public class PortfolioBalanceDTO {

    private String asset;
    private double quantity;

    public PortfolioBalanceDTO(String asset, double quantity) {
        this.asset = asset;
        this.quantity = quantity;
    }

    public String getAsset() {
        return asset;
    }

    public double getQuantity() {
        return quantity;
    }

    // Helps print readable logs
    @Override
    public String toString() {
        return "PortfolioBalanceDTO{" +
                "asset='" + asset + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}