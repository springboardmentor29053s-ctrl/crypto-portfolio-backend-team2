package com.crypto_portfolio_tracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EtherscanService {

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final String API_KEY = "YOUR_API_KEY";

    public static boolean isContractVerified(String contractAddress) {
        try {
            String url = "https://api.etherscan.io/v2/api"
                    + "?module=contract"
                    + "&action=getsourcecode"
                    + "&address=" + contractAddress
                    + "&apikey=" + API_KEY;

            Map<String, Object> response =
                    restTemplate.getForObject(url, Map.class);

            Object resultObj = response.get("result");

            if (resultObj instanceof List<?> resultList && !resultList.isEmpty()) {

                Map<?, ?> contractData = (Map<?, ?>) resultList.get(0);
                String sourceCode = (String) contractData.get("SourceCode");

                return sourceCode != null && !sourceCode.isEmpty();
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

