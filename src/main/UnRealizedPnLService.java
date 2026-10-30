package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.model.Holding;
import com.crypto_portfolio_tracker.model.UserEntity;
import com.crypto_portfolio_tracker.repository.HoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class UnRealizedPnLService {

    private final HoldingRepository holdingRepo;

    public UnRealizedPnLService(HoldingRepository holdingRepo) {
        this.holdingRepo = holdingRepo;
    }


    public BigDecimal getUnRealizedPnl(UserEntity user){

        List<Holding> holdings = holdingRepo.findByUser(user);
        BigDecimal total = BigDecimal.ZERO;

        for(Holding hold : holdings){
            String asset = hold.getAsset();

            if ("USDT".equalsIgnoreCase(asset)) {
                continue;
            }
            String symbol = asset + "USDT";
            BigDecimal marketPrice = getMarketPrice(symbol);
            BigDecimal pnl = marketPrice.
                    subtract(hold.getAverageCost()).
                    multiply(hold.getQuantity());

            total = total.add(pnl);
        }
        return total;
    }

    private BigDecimal getMarketPrice(String symbol) {

        String url = "https://testnet.binance.vision/api/v3/ticker/price?symbol=" + symbol;
        RestTemplate restTemplate = new RestTemplate();
       Map<String, String> response = restTemplate.getForObject(url, Map.class);
       return new BigDecimal(response.get("price"));
    }
}


