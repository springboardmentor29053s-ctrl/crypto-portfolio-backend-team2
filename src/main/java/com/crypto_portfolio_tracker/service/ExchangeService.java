package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.ExchangeResponseDTO;
import com.crypto_portfolio_tracker.model.Exchange;
import com.crypto_portfolio_tracker.repository.ExchangeRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ExchangeService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final ExchangeRepository exchangeRepository;

    public ExchangeService(ExchangeRepository exchangeRepository){
        this.exchangeRepository = exchangeRepository;
    }

    @Value("${coingecko.api.exchanges}")
    private String exchangeUrl;

    public List<Exchange> getAll() {
        return exchangeRepository.findAll();
    }

    public Exchange create(String name, String baseUrl) {
        Exchange exchange = new Exchange();
        exchange.setName(name);
        exchange.setBaseUrl(baseUrl);
        return exchangeRepository.save(exchange);
    }

    public List<ExchangeResponseDTO> getExchanges(int page, int size){
        URI uri = UriComponentsBuilder
                .fromHttpUrl(exchangeUrl)
                .queryParam("per_page", size)
                .queryParam("page", page)
                .build()
                .toUri();

        ExchangeResponseDTO[] response =
                restTemplate.getForObject(uri, ExchangeResponseDTO[].class);

        if (response == null) {
            return List.of();
        }
        return Arrays.asList(response);
    }
}
