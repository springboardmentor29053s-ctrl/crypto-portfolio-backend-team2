package com.example.backend.service;

import com.example.backend.dto.ExchangeRequest;
import com.example.backend.entity.ApiKey;
import com.example.backend.entity.Exchange;
import com.example.backend.entity.User;
import com.example.backend.repository.ApiKeyRepository;
import com.example.backend.repository.ExchangeRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExchangeService {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    public String connectExchange(ExchangeRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exchange exchange = exchangeRepository.findById(request.getExchangeId())
                .orElseThrow(() -> new RuntimeException("Exchange not found"));

        // check if already connected, then update existing
        Optional<ApiKey> existingApiKey = apiKeyRepository.findByUserAndExchange(user, exchange);

        ApiKey apiKey;
        if (existingApiKey.isPresent()) {
            apiKey = existingApiKey.get();
        } else {
            apiKey = new ApiKey();
            apiKey.setUser(user);
            apiKey.setExchange(exchange);
        }

        // IMPORTANT FIX: encrypt both apiKey and apiSecret
        apiKey.setApiKey(CryptoUtils.encrypt(request.getApiKey()));
        apiKey.setApiSecret(CryptoUtils.encrypt(request.getApiSecret()));
        apiKey.setLabel(request.getLabel());

        apiKeyRepository.save(apiKey);

        return "Exchange connected successfully!";
    }

    public List<Exchange> getAllExchanges() {
        return exchangeRepository.findAll();
    }

    public List<ApiKey> getConnectedExchanges(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return apiKeyRepository.findByUser(user);
    }

    public void disconnectExchange(Long exchangeId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ApiKey apiKey = apiKeyRepository
                .findByUserAndExchangeId(user, exchangeId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        apiKeyRepository.delete(apiKey);
    }
}