package demo.Crypto_Portfolio.app.dto.portfolio;

public class RiskAlertDTO {

    private String assetSymbol;
    private String message;
    private String severity;

    public RiskAlertDTO() {}

    public RiskAlertDTO(String assetSymbol, String message, String severity) {
        this.assetSymbol = assetSymbol;
        this.message = message;
        this.severity = severity;
    }


    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}