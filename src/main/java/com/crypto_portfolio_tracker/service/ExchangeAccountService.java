package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.BinanceAccountResponse;
import com.crypto_portfolio_tracker.dto.OpenOrder;
import com.crypto_portfolio_tracker.dto.OrderRequestDTO;
import com.crypto_portfolio_tracker.dto.TradeDTO;
import com.crypto_portfolio_tracker.model.*;
import com.crypto_portfolio_tracker.repository.*;
import com.crypto_portfolio_tracker.util.CryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExchangeAccountService {

    private final ExchangeRepository exchangeRepository;
    private final ExchangeAccountRepository exchangeAccountRepository;
    private final TradeRecordRepository tradeRecordRepo;
    private final ExchangeConnectorFactory exchangeConnectorFactory;
    private final CryptoUtil cryptoUtil;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final BinanceBalanceRepository binanceBalanceRepo;

    public ExchangeAccountService(ExchangeRepository exchangeRepository,
                                  ExchangeAccountRepository exchangeAccountRepository, TradeRecordRepository tradeRecordRepo,
                                  ExchangeConnectorFactory exchangeConnectorFactory,
                                  CryptoUtil cryptoUtil, UserRepository userRepository, OrderService orderService, BinanceBalanceRepository binanceBalanceRepo) {
        this.exchangeRepository = exchangeRepository;
        this.exchangeAccountRepository = exchangeAccountRepository;
        this.tradeRecordRepo = tradeRecordRepo;
        this.exchangeConnectorFactory = exchangeConnectorFactory;
        this.cryptoUtil = cryptoUtil;
        this.userRepository = userRepository;
        this.orderService = orderService;
        this.binanceBalanceRepo = binanceBalanceRepo;
    }

    /// Todo: Connect user to exchange by saving encrypted API keys in the database
    public void connect(
            Integer userId,
            String exchangeName,
            String apiKey,
            String secretKey){

        Exchange exchange = exchangeRepository.findByNameIgnoreCase(exchangeName)
                .orElseThrow(() -> new RuntimeException("Exchange not found"));

        // Check if already connected
        Optional<ExchangeAccount> existing =
                exchangeAccountRepository
                        .findByUserIdAndExchangeNameIgnoreCase(userId, exchangeName);

        if (existing.isPresent()) {
            throw new RuntimeException("Exchange already connected");
        }

        ExchangeAccount exchangeAccount = new ExchangeAccount();
        exchangeAccount.setUserId(userId);
        exchangeAccount.setExchange(exchange);


        //Encrypt API keys before saving to the database
        String encryptedApiKey = cryptoUtil.encrypt(apiKey);
        String encryptedSecretKey = cryptoUtil.encrypt(secretKey);

        exchangeAccount.setApi_key(encryptedApiKey);
        exchangeAccount.setApi_secret(encryptedSecretKey);

        exchangeAccountRepository.save(exchangeAccount);
    }

    //placing order
    public OpenOrder placeOrderForUserByEmail(String email, OrderRequestDTO requestDTO) throws Exception {

        log.info("USER ID = {}", email);
        log.info("REQUEST EXCHANGE = '{}'", requestDTO.getExchangeName());

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ExchangeAccount account =
                getByUserIdAndExchange(Math.toIntExact(user.getId()), requestDTO.getExchangeName());

        String decryptedApiKey = cryptoUtil.decrypt(account.getApi_key());
        String decryptedSecretKey = cryptoUtil.decrypt(account.getApi_secret());

        ExchangeConnector connector =
                exchangeConnectorFactory.getConnector(requestDTO.getExchangeName());

        log.info("ORDER TYPE RECEIVED = {}", requestDTO.getType());
        OpenOrder orderResponse;
        if("MARKET".equalsIgnoreCase(requestDTO.getType())){
            orderResponse = ((BinanceConnectorImpl) connector).placeOrder(
                    decryptedApiKey,
                    decryptedSecretKey,
                    requestDTO.getSymbol(),
                    requestDTO.getSide(),
                    "MARKET",
                    requestDTO.getQuantity(),
                    null
            );
        } else if("LIMIT".equalsIgnoreCase(requestDTO.getType())){
            if (requestDTO.getPrice() == null || requestDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Price required for LIMIT order");
            }
            orderResponse= ((BinanceConnectorImpl) connector).placeOrder(
                    decryptedApiKey,
                    decryptedSecretKey,
                    requestDTO.getSymbol(),
                    requestDTO.getSide(),
                    "LIMIT",
                    requestDTO.getQuantity(),
                    String.valueOf(requestDTO.getPrice())
            );
        }
            else {
            throw new RuntimeException("Unknown order type");
        }
        orderService.saveOrder(
           user.getId(), requestDTO.getExchangeName(), orderResponse);

        return orderResponse;
    }

    /// TODO: add symbol parameter to get balances for specific trading pair
    public List<BinanceBalance> getBalancesForUserByEmail(String email, String exchangeName) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return binanceBalanceRepo.findByUser(user)
                .stream()
                .sorted((a,b) -> b.getFree().compareTo(a.getFree()))
                .toList();
    }

    public List<BinanceBalance> getStoredBalances(Integer userId) {

        UserEntity user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        return binanceBalanceRepo.findByUser(user);
    }

    @Transactional
    public void syncBalancesForUser(Integer userId, String exchangeName) {

        ExchangeAccount account = getByUserIdAndExchange(userId, exchangeName);

        String decryptedApiKey = cryptoUtil.decrypt(account.getApi_key());
        String decryptedSecretKey = cryptoUtil.decrypt(account.getApi_secret());

        ExchangeConnector connector =
                exchangeConnectorFactory.getConnector(exchangeName);

        // DTO list from Binance
        List<BinanceAccountResponse.Balance> dtoBalances =
                connector.getBalances(decryptedApiKey, decryptedSecretKey);

        UserEntity user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BinanceBalance> entityBalances = dtoBalances.stream()
                .filter(dto ->
                        new BigDecimal(dto.getFree()).compareTo(BigDecimal.ZERO) > 0 ||
                        new BigDecimal(dto.getLocked()).compareTo(BigDecimal.ZERO) > 0
                )
                .map(dto -> {
                    BinanceBalance entity = new BinanceBalance();
                    entity.setAsset(dto.getAsset());
                    entity.setFree(new BigDecimal(dto.getFree()));
                    entity.setLocked(new BigDecimal(dto.getLocked()));
                    entity.setUser(user);
                    entity.setLastUpdated(LocalDateTime.now());
                    return entity;
                })
                .collect(Collectors.toList());

        log.info("About to delete balances for user {}", user.getId());
        binanceBalanceRepo.deleteAllByUserId(Long.valueOf(userId));
        log.info("Delete executed");//single sql query
        binanceBalanceRepo.saveAll(entityBalances);

    }

    @Transactional
    public void syncBalancesForUserByEmail(String email, String exchangeName) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        syncBalancesForUser(Math.toIntExact(user.getId()), exchangeName);
    }

    /// TODO: add symbol parameter to get open orders for specific trading pair
    public List<?> getOpenOrdersForUser(Integer userId, String exchangeName, String symbol) {

        ExchangeAccount account = getByUserIdAndExchange(userId, exchangeName);

        String decryptedApiKey = cryptoUtil.decrypt(account.getApi_key());
        String decryptedSecretKey = cryptoUtil.decrypt(account.getApi_secret());

        ExchangeConnector connector =
                exchangeConnectorFactory.getConnector(exchangeName);

        return connector.getOpenOrders(decryptedApiKey, decryptedSecretKey, symbol);
    }

    public List<?> getOpenOrdersForUserByEmail(String email, String exchangeName, String symbol) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return getOpenOrdersForUser(Math.toIntExact(user.getId()), exchangeName, symbol);
    }


    public ExchangeAccount getByUserIdAndExchange(Integer userId, String exchangeName) {
        String normalized = exchangeName.trim().toUpperCase();
        // Find exchange by name
        exchangeRepository
                .findByNameIgnoreCase(normalized)
                .orElseThrow(() ->
                        new RuntimeException("Exchange not found"));

        // 2 Find exchange account
        return exchangeAccountRepository
                .findByUserIdAndExchangeNameIgnoreCase(userId, exchangeName)
                .orElseThrow(() ->
                        new RuntimeException("Exchange account not connected"));
    }

    public List<TradeDTO> getMyTradesByEmail(String email, String symbol) {
      UserEntity user = userRepository.findByEmail(email)
              .orElseThrow(() -> new RuntimeException("User not found"));

      List<TradeRecord>  trades = tradeRecordRepo
              .findByUserAndSymbol(user, symbol);
      return trades.stream()
              .map(this::mapToDto)
              .toList();
    }

   /// fetch trades from DB and map them to DTO.
    public TradeDTO mapToDto(TradeRecord t) {
        return TradeDTO.builder()
                .tradeId(t.getTradeId())
                .symbol(t.getSymbol())
                .side(t.getSide())
                .price(t.getPrice())
                .quantity(t.getQuantity())
                .commission(t.getCommission())
                .commissionAsset(t.getCommissionAsset())
                .tradeTime(t.getTradeTime())
                .build();
    }

}
