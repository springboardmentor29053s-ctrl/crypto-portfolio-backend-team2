package demo.Crypto_Portfolio.app.service;

import demo.Crypto_Portfolio.app.model.ApiKey;
import demo.Crypto_Portfolio.app.model.Holding;
import demo.Crypto_Portfolio.app.model.Trade;

import demo.Crypto_Portfolio.app.repository.ApiKeyRepository;
import demo.Crypto_Portfolio.app.repository.HoldingRepository;


import demo.Crypto_Portfolio.app.dto.exchange.ConnectExchangeRequest;
import demo.Crypto_Portfolio.app.dto.portfolio.PortfolioBalanceDTO;

import demo.Crypto_Portfolio.app.service.exchange.BinanceConnector;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeService {


    private final BinanceConnector binanceConnector;
    private final HoldingRepository holdingRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final EncryptionService encryptionService;
    private final PortfolioService portfolioService;
    private final CryptoService cryptoService;

    public ExchangeService(ApiKeyRepository apiKeyRepository,
                           EncryptionService encryptionService,
                           BinanceConnector binanceConnector,
                           HoldingRepository holdingRepository,
                           CryptoService cryptoService,
                           PortfolioService portfolioService) {

        this.apiKeyRepository = apiKeyRepository;
        this.encryptionService = encryptionService;
        this.binanceConnector = binanceConnector;
        this.holdingRepository = holdingRepository;
        this.cryptoService = cryptoService;
        this.portfolioService = portfolioService;
    }

// =========================
// CONNECT EXCHANGE
// =========================

    public String connectExchange(ConnectExchangeRequest request) {

        if(request.getApiKey() == null || request.getApiKey().isBlank()){
            throw new IllegalArgumentException("API key and Secret key are required");
        }

        if(request.getSecret() == null || request.getSecret().isBlank()){
            throw new IllegalArgumentException("API key and Secret key are required");
        }

        boolean valid = binanceConnector.validateApiKey(
                request.getApiKey(),
                request.getSecret()
        );

        if(!valid){
            throw new IllegalArgumentException("Invalid Binance API Key or Secret Key");
        }

        Optional<ApiKey> existingKey =
                apiKeyRepository.findByUserIdAndExchangeId(
                        request.getUserId(),
                        request.getExchangeId()
                );

        ApiKey key;

        if (existingKey.isPresent()) {
            key = existingKey.get();
        } else {

            key = new ApiKey();
            key.setUserId(request.getUserId());
            key.setExchangeId(request.getExchangeId());
            key.setCreatedAt(LocalDateTime.now());
        }

        key.setApiKeyEncrypted(
                encryptionService.encrypt(request.getApiKey())
        );

        key.setSecretEncrypted(
                encryptionService.encrypt(request.getSecret())
        );

        apiKeyRepository.save(key);

        return "Exchange connected successfully";
    }

// =========================
// SYNC PORTFOLIO FROM BINANCE
// =========================

    @Transactional
    public String syncPortfolio(Long userId, Long exchangeId) {

        ApiKey apiKey = apiKeyRepository
                .findByUserIdAndExchangeId(userId, exchangeId)
                .orElseThrow(() -> new RuntimeException("Exchange not connected"));

        String apiKeyValue =
                encryptionService.decrypt(apiKey.getApiKeyEncrypted());

        String secretValue =
                encryptionService.decrypt(apiKey.getSecretEncrypted());

        // Fetch balances from Binance
        List<PortfolioBalanceDTO> balances =
                binanceConnector.fetchBalances(apiKeyValue, secretValue);

        System.out.println("Balances received from Binance: " + balances);

        // Delete old holdings
        holdingRepository.deleteByUserId(userId);

        // Insert fresh holdings
        for (PortfolioBalanceDTO balance : balances) {

            if (balance.getQuantity() <= 0) continue;

            Holding holding = new Holding();

            holding.setUserId(userId);
            holding.setAssetSymbol(balance.getAsset());
            holding.setQuantity(balance.getQuantity());

            // Fetch live price for avg cost
            double livePrice = cryptoService.getLivePrice(balance.getAsset());
            holding.setAvgCost(livePrice);

            holding.setCoinId(convertSymbolToId(balance.getAsset()));
            holding.setUpdatedAt(LocalDateTime.now());

            holdingRepository.save(holding);
        }

        return "Portfolio synced successfully from Binance";
    }

// =========================
// SYNC TRADES FROM BINANCE
// =========================

    public String syncTrades(Long userId, Long exchangeId) {

        ApiKey apiKey = apiKeyRepository
                .findByUserIdAndExchangeId(userId, exchangeId)
                .orElseThrow(() -> new RuntimeException("Exchange not connected"));

        String apiKeyValue =
                encryptionService.decrypt(apiKey.getApiKeyEncrypted());

        String secretValue =
                encryptionService.decrypt(apiKey.getSecretEncrypted());

        String[] symbols = {"BTCUSDT","ETHUSDT","SOLUSDT"};

        for(String symbol : symbols){

            JSONArray trades =
                    binanceConnector.fetchTrades(apiKeyValue, secretValue, symbol);

            for(int i=0;i<trades.length();i++){

                JSONObject t = trades.getJSONObject(i);

                Trade trade = new Trade();

                trade.setSymbol(symbol.replace("USDT",""));
                trade.setPrice(t.getDouble("price"));
                trade.setQuantity(t.getDouble("qty"));

                if(t.getBoolean("isBuyer"))
                    trade.setSide("BUY");
                else
                    trade.setSide("SELL");

                trade.setExecutedAt(LocalDateTime.now());


                portfolioService.saveTrade(userId, trade);
            }
        }

        return "Trades synced successfully";
    }

// =========================
// SYMBOL → COINGECKO ID
// =========================

    private String convertSymbolToId(String symbol) {

        switch (symbol.toUpperCase()) {

            case "BTC": return "bitcoin";
            case "ETH": return "ethereum";
            case "SOL": return "solana";
            case "BNB": return "binancecoin";
            case "USDT": return "tether";
            case "XRP": return "ripple";
            case "ADA": return "cardano";
            case "DOGE": return "dogecoin";
            case "DOT": return "polkadot";
            case "MATIC": return "matic-network";
            case "TRX": return "tron";
            case "LTC": return "litecoin";

            default: return symbol.toLowerCase();
        }
    }

}
