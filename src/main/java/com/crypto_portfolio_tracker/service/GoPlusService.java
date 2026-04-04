package com.crypto_portfolio_tracker.service;

import lombok.Data;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class GoPlusService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Cacheable("token-security")
    public Map<String, Object> getTokenSecurity(String contractAddress) {

        try {
            Thread.sleep(300);
            String url = "https://api.gopluslabs.io/api/v1/token_security/1?contract_addresses="
                    + contractAddress;

            Map<String, Object> response =
                    restTemplate.getForObject(url, Map.class);

            if (response == null) return null;
            Map<String, Object> result =
                    (Map<String, Object>) response.get("result");

            if (result == null) return null;
            return (Map<String, Object>) result.get(contractAddress);

        } catch (Exception e) {
            return null;
        }
    }
}
