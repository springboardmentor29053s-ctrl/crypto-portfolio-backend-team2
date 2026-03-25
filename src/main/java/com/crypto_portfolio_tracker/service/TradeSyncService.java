package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.TradeDTO;
import com.crypto_portfolio_tracker.model.TradeRecord;
import com.crypto_portfolio_tracker.model.UserEntity;
import com.crypto_portfolio_tracker.repository.TradeRecordRepository;
import com.crypto_portfolio_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TradeSyncService {

    private final TradeRecordRepository tradeRecordRepo;
    private final ExchangeAccountService exchangeAccountService;
    private final UserRepository userRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoSyncTrades() {

        List<UserEntity> users = userRepository.findAll();

        for (UserEntity user : users) {

            String email = user.getEmail();

            List<String> symbols =
                    tradeRecordRepo.findDistinctSymbolsByUser(user);

            for (String symbol : symbols) {

                List<TradeDTO> trades =
                        exchangeAccountService.getMyTradesByEmail(email, symbol);

                //  Fetch all existing tradeIds once
                List<Long> existingIds =
                        tradeRecordRepo.findTradeIdsByUserAndSymbol(user, symbol);

                Set<Long> existingSet = new HashSet<>(existingIds);

                List<TradeRecord> newRecords = new ArrayList<>();

                for (TradeDTO t : trades) {

                    if (!existingSet.contains(t.getTradeId())) {

                        TradeRecord record = TradeRecord.builder()
                                .tradeId(t.getTradeId())
                                .symbol(t.getSymbol())
                                .price(t.getPrice())
                                .quantity(t.getQuantity())
                                .commission(t.getCommission())
                                .commissionAsset(t.getCommissionAsset())
                                .side(t.isBuyer() ? "BUY" : "SELL")
                                .tradeTime(t.getTradeTime())
                                .user(user)
                                .build();

                        newRecords.add(record);
                    }
                }

                //  Save all new trades in ONE query
                if (!newRecords.isEmpty()) {
                    tradeRecordRepo.saveAll(newRecords);
                }
            }
        }
    }

    public List<TradeDTO> getTradesForUserAndSymbol(UserEntity user, String symbol) {
        List<TradeRecord> trades = tradeRecordRepo.findByUserAndSymbol(user, symbol);
        return trades.stream()
                .map(t -> new TradeDTO(
                        t.getUser().getId(),
                        t.getTradeId(),
                        t.getSymbol(),
                        t.getSide(),
                        t.getPrice(),
                        t.getQuantity(),
                        t.getCommission(),
                        t.getCommissionAsset(),
                        t.getTradeTime()
                ))
                .toList();
    }
}












/*
/    @Transactional
//    public void syncTrades(String email, String symbol){
//
//        UserEntity user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        List<TradeDTO> trades = exchangeAccountService.getMyTradesByEmail(email, symbol);
//
//        for(TradeDTO t : trades){
//            if(!tradeRecordRepo.existsByTradeId(t.getTradeId())){
//                TradeRecord record = TradeRecord.builder()
//                        .tradeId(t.getTradeId())
//                        .symbol(symbol)
//                        .price(t.getPrice())
//                        .quantity(t.getQuantity())
//                        .commission(t.getCommission())
//                        .commissionAsset(t.getCommissionAsset())
//                        .side(t.isBuyer() ? "BUY" : "SELL")
//                        .tradeTime(t.getTradeTime())
//                        .user(user)
//                        .build();
//                tradeRecordRepo.save(record);
//            }
//        }
//    }


 */
