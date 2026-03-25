package demo.Crypto_Portfolio.app.dto.exchange;

public class ConnectExchangeRequest {

    private Long userId;
    private Long exchangeId;
    private String apiKey;
    private String secret;

    public Long getUserId() {
        return userId;
    }

    public Long getExchangeId() {
        return exchangeId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecret() {
        return secret;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setExchangeId(Long exchangeId) {
        this.exchangeId = exchangeId;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}