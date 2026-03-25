package demo.Crypto_Portfolio.app.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import demo.Crypto_Portfolio.app.repository.ScamTokenRepository;

@Service
public class ScamService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ScamTokenRepository scamTokenRepository;

    // 🔥 GoPlus API
    private static final String GOPLUS_API =
            "https://api.gopluslabs.io/api/v1/token_security/1?contract_addresses=%s";

    public boolean isScamToken(String contractAddress) {

        try {

            // ✅ Safety check
            if (contractAddress == null || contractAddress.isEmpty()) {
                return false;
            }

            // =============================
            // 1️⃣ CHECK DB (ScamTokens Table)
            // =============================
            if (scamTokenRepository.existsByContractAddress(contractAddress)) {
                System.out.println("⚠️ Scam detected from DB blacklist");
                return true;
            }

            // =============================
            // 2️⃣ CALL GOPLUS API
            // =============================
            String url = String.format(GOPLUS_API, contractAddress);

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || response.get("result") == null) {
                return false;
            }

            Map resultMap = (Map) response.get("result");

            // 🔥 IMPORTANT FIX (lowercase)
            Map result = (Map) resultMap.get(contractAddress.toLowerCase());

            if (result == null) {
                return false;
            }

            String isHoneyPot = (String) result.get("is_honeypot");
            String isBlacklisted = (String) result.get("is_blacklisted");

            // =============================
            // 3️⃣ FINAL DECISION
            // =============================
            if ("1".equals(isHoneyPot) || "1".equals(isBlacklisted)) {

                System.out.println("🚨 Scam detected via GoPlus API");

                // OPTIONAL: Save to DB for future
                // (only if you want caching)
                // You can skip this if not needed

                return true;
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}