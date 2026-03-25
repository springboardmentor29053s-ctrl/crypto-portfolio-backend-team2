package demo.Crypto_Portfolio.app.controller;

import demo.Crypto_Portfolio.app.dto.trade.TradeOrderRequest;
import demo.Crypto_Portfolio.app.model.Trade;
import demo.Crypto_Portfolio.app.service.PortfolioService;
import demo.Crypto_Portfolio.app.service.CryptoService;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final PortfolioService portfolioService;
    private final CryptoService cryptoService;

    public TradeController(PortfolioService portfolioService,
                           CryptoService cryptoService) {

        this.portfolioService = portfolioService;
        this.cryptoService = cryptoService;
    }

    // ================================
    // PLACE BUY / SELL ORDER
    // ================================

    @PostMapping("/order")
    public Trade placeOrder(
            @RequestParam Long userId,
            @RequestBody TradeOrderRequest request) {

        Trade trade = new Trade();

        trade.setSymbol(request.getAssetSymbol());
        trade.setSide(request.getSide());
        trade.setQuantity(request.getQuantity());

        // fetch LIVE price from CoinGecko
        double livePrice = cryptoService.getLivePrice(request.getAssetSymbol());
        trade.setPrice(livePrice);

        trade.setExecutedAt(LocalDateTime.now());

        return portfolioService.saveTrade(userId, trade);
    }
}