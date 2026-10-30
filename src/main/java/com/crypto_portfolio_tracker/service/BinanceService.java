package com.crypto_portfolio_tracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class BinanceService {

     private final RestTemplate restTemplate = new RestTemplate();

     public String getBTCPrice(){

         String url = "https://api.coingecko.com/api/v3/exchanges";
         log.info("url {}", url);
         return restTemplate.getForObject(url,String.class);
     }
}
