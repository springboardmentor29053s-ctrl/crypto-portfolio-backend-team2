package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.PricingDTO;
import com.crypto_portfolio_tracker.dto.RiskAlertDTO;
import com.crypto_portfolio_tracker.enums.AlertType;
import com.crypto_portfolio_tracker.model.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RiskAnalysisService {

    private final CryptoScamService scamService ;
    private final NotificationService notificationService ;
    private final GoPlusService goPlusService;

    public RiskAnalysisService(CryptoScamService scamService,
                               NotificationService notificationService, GoPlusService goPlusService) {
        this.scamService = scamService;
        this.notificationService = notificationService;
        this.goPlusService = goPlusService;
    }

    public  List<RiskAlertDTO> analyzeWithLiveRisk(
            List<PricingDTO> prices,
            UserEntity user
    ) {

        List<RiskAlertDTO> alerts = new ArrayList<>();

        for (PricingDTO p : prices) {

            RiskAlertDTO dto = new RiskAlertDTO();
            dto.setAsset(p.name);
            dto.setSymbol(p.symbol);
            dto.setPrice(p.price);
            dto.setChange24h(p.getChange24h());
            dto.setMarketCap(p.marketCap);

            String contract = p.contractAddress;

            //  1. HIGH RISK (Top priority)
            if (contract != null && scamService.isScamAndStore(contract)) {

                dto.setRiskLevel("HIGH");
                dto.setReason("Scam token");

                notificationService.createRiskAlert(
                        user, p.symbol,
                        AlertType.RUGPULL_WARNING,
                        "Scam detected"
                );
                log.info("DB Scam hit: {}", contract);
                alerts.add(dto);
                continue;
            }
            //low market cap and price drop < -20 and new listed token
            //is a suspicious
            if (p.marketCap < 50_000_000 && p.change24h < -20) {
                dto.setRiskLevel("HIGH");
                dto.setReason("Possible rug pull (trend)");
                alerts.add(dto);
                continue;
            }

            //  2. MEDIUM RISK
            if (p.contractAddress != null) {

                Map<String, Object> security;
                security = goPlusService.getTokenSecurity(p.contractAddress);

                if (security != null) {

                    String honeypot = (String) security.get("is_honeypot");
                    String owner = (String) security.get("owner_percent");

                    if ("1".equals(honeypot)) {
                        dto.setRiskLevel("HIGH");
                        dto.setReason("Honeypot");
                        alerts.add(dto);
                        continue;
                    }

                    if (owner != null) {
                        try {
                            if (Double.parseDouble(owner) > 50) {
                                dto.setRiskLevel("MEDIUM");
                                dto.setReason("High owner control");
                                alerts.add(dto);
                                continue;
                            }
                        } catch (Exception ignored) {}
                    }
                    log.info("Symbol: {}, Contract: {}", p.symbol, contract);
                }
            }

            if (Math.abs(p.change24h) > 8) {
                dto.setRiskLevel("MEDIUM");
                dto.setReason("High volatility");
                alerts.add(dto);
                continue;
            }

            //  3. LOW RISK (default)
            dto.setRiskLevel("LOW");
            dto.setReason("Stable asset");

            alerts.add(dto);
        }
        return alerts;
    }
}














/*

HIGH Risk
Scam tokens
Honeypots
Rug pull tokens
Crash >15%
   MEDIUM Risk
Small cap coins
High volatility
Unverified contracts
Proxy contracts
   LOW Risk
BTC, ETH, SOL
Large-cap stable assets
 */











