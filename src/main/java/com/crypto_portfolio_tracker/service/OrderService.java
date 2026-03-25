package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.FillDTO;
import com.crypto_portfolio_tracker.dto.OpenOrder;
import com.crypto_portfolio_tracker.dto.OrderExecutionResponse;
import com.crypto_portfolio_tracker.model.*;
import com.crypto_portfolio_tracker.repository.*;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
public class OrderService {
    private final OpenOrderRepository openOrderRepository;
    private final ExchangeAccountRepository exchangeAccountRepository;
    private final PortfolioService portfolioService;
    private final UserRepository userRepository;
    private final TradeRecordRepository tradeRecordRepo;
    private final HoldingRepository holdingRepo;

    public OrderService(OpenOrderRepository openOrderRepository,
                        ExchangeAccountRepository exchangeAccountRepository, PortfolioService portfolioService, TradeRecordRepository tradeRecordRepo, UserRepository userRepository, TradeRecordRepository tradeRecordRepository, HoldingRepository holdingRepo) {

        this.openOrderRepository = openOrderRepository;
        this.exchangeAccountRepository = exchangeAccountRepository;
        this.portfolioService = portfolioService;
        this.tradeRecordRepo = tradeRecordRepo;
        this.userRepository = userRepository;
        this.holdingRepo = holdingRepo;
    }

    @Transactional
    public OrderExecutionResponse saveOrder(Long userId, String exchangeName, OpenOrder order) {

        ExchangeAccount account = exchangeAccountRepository
                .findByUserIdAndExchangeNameIgnoreCase(Math.toIntExact(userId), exchangeName)
                .orElseThrow(() -> new RuntimeException("Exchange account not found"));

        OpenOrderEntity entity = getOpenOrderEntity(order, account);

        UserEntity user = userRepository.getReferenceById(userId);

        // If no fills (LIMIT not executed)
        if (order.getFills() == null || order.getFills().isEmpty()) {
            openOrderRepository.save(entity);

            return OrderExecutionResponse.builder()
                    .symbol(order.getSymbol())
                    .side(order.getSide())
                    .status(order.getStatus())
                    .orderId(order.getOrderId())
                    .executedQty(BigDecimal.ZERO)
                    .build();
        }

        FillDTO fill = order.getFills().get(0);
        BigDecimal executedQty = fill.getQty();
        BigDecimal executedPrice = fill.getPrice();

        for (FillDTO f : order.getFills()) {

            if (!tradeRecordRepo.existsByTradeId(f.getTradeId())) {

                TradeRecord trade = TradeRecord.builder()
                        .tradeId(f.getTradeId())
                        .orderId(order.getOrderId())
                        .symbol(order.getSymbol())
                        .side(order.getSide())
                        .price(f.getPrice())
                        .quantity(f.getQty())
                        .commission(f.getCommission())
                        .commissionAsset(f.getCommissionAsset())
                        .tradeTime(order.getTransactTime())
                        .user(user)
                        .build();

                tradeRecordRepo.save(trade);
                portfolioService.updateHolding(user, trade);
            }
        }

        openOrderRepository.save(entity);

        Holding holding = holdingRepo
                .findByUserAndAsset(user, extractBaseAsset(order.getSymbol()))
                .orElse(Holding.builder()
                        .user(user)
                        .asset(extractBaseAsset(order.getSymbol()))
                        .quantity(BigDecimal.ZERO)
                        .averageCost(BigDecimal.ZERO)
                        .realizedProfitLoss(BigDecimal.ZERO)
                        .build());

        return OrderExecutionResponse.builder()
                .symbol(order.getSymbol())
                .side(order.getSide())
                .status(order.getStatus())
                .orderId(order.getOrderId())
                .tradeId(fill.getTradeId())
                .executedQty(executedQty)
                .executedPrice(executedPrice)
                .portfolioQuantity(holding.getQuantity())
                .averageCost(holding.getAverageCost())
                .build();
    }
    
    @Nonnull
    private static OpenOrderEntity getOpenOrderEntity(OpenOrder order, ExchangeAccount account) {
        OpenOrderEntity entity = new OpenOrderEntity();

        entity.setExchangeAccount(account);
        entity.setSymbol(order.getSymbol());
        entity.setSide(order.getSide());
        entity.setType(order.getType());
        entity.setStatus(order.getStatus());
        entity.setPrice(order.getPrice());
        entity.setOrigQty(order.getOrigQty());
        entity.setExecutedQty(order.getExecutedQty());
        entity.setOrderId(order.getOrderId());
        entity.setTransactTime(order.getTransactTime());
        return entity;
    }

    private String extractBaseAsset(String symbol) {
        return symbol.replace("USDT", "");
    }
}