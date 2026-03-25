package demo.Crypto_Portfolio.app.dto.exchange;

public class ExchangePortfolioDTO {

    private String asset;
    private double quantity;

    public ExchangePortfolioDTO(String asset, double quantity) {
        this.asset = asset;
        this.quantity = quantity;
    }

    public String getAsset() {
        return asset;
    }

    public double getQuantity() {
        return quantity;
    }
}