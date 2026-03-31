package com.example.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CryptoService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getTopCoins() {

        String url = "https://api.coingecko.com/api/v3/coins/markets"
                + "?vs_currency=usd"
                + "&per_page=1"
                + "&page=1"
                + "&sparkline=false";

        return restTemplate.getForObject(url, String.class);
    }
}
