package com.example.backend.service;

import com.example.backend.dto.ExchangeRequest;
import com.example.backend.entity.ApiKey;
import com.example.backend.entity.Exchange;
import com.example.backend.entity.User;
import com.example.backend.repository.ApiKeyRepository;
import com.example.backend.repository.ExchangeRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.utils.CryptoUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExchangeService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final ExchangeRepository exchangeRepository;

    public ExchangeService(ApiKeyRepository apiKeyRepository,
                           UserRepository userRepository,
                           ExchangeRepository exchangeRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
        this.exchangeRepository = exchangeRepository;
    }

    public String connectExchange(ExchangeRequest request, String email) {
        if (request.getExchangeId() == null) {
            throw new RuntimeException("Exchange is required");
        }
        if (request.getApiKey() == null || request.getApiKey().isBlank()) {
            throw new RuntimeException("API key is required");
        }
        if (request.getApiSecret() == null || request.getApiSecret().isBlank()) {
            throw new RuntimeException("API secret is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exchange exchange = exchangeRepository.findById(request.getExchangeId())
                .orElseThrow(() -> new RuntimeException("Exchange not found"));

        Optional<ApiKey> existingApiKey = apiKeyRepository.findByUserAndExchange(user, exchange);

        ApiKey apiKey = existingApiKey.orElseGet(ApiKey::new);
        apiKey.setUser(user);
        apiKey.setExchange(exchange);
        apiKey.setApiKey(CryptoUtils.encrypt(request.getApiKey().trim()));
        apiKey.setApiSecret(CryptoUtils.encrypt(request.getApiSecret().trim()));
        apiKey.setLabel(
                request.getLabel() == null || request.getLabel().isBlank()
                        ? exchange.getName() + " Account"
                        : request.getLabel().trim()
        );

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

        ApiKey apiKey = apiKeyRepository.findByUserAndExchangeId(user, exchangeId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        apiKeyRepository.delete(apiKey);
    }
}