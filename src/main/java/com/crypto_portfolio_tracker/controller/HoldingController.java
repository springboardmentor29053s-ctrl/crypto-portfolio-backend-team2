package com.crypto_portfolio_tracker.controller;

import com.crypto_portfolio_tracker.dto.HoldingDTO;
import com.crypto_portfolio_tracker.model.UserEntity;
import com.crypto_portfolio_tracker.repository.HoldingRepository;
import com.crypto_portfolio_tracker.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RestController
public class HoldingController {

    private final UserRepository userRepository;
    private final HoldingRepository holdingRepo;


    public HoldingController(UserRepository userRepository, HoldingRepository holdingRepos) {
        this.userRepository = userRepository;
        this.holdingRepo = holdingRepos;

    }

    @GetMapping("/portfolio")
    public ResponseEntity<List<HoldingDTO>> getHoldings(Authentication authentication) {
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<HoldingDTO> holdings = holdingRepo.findByUser(user)
                .stream()
                .map(holding -> new HoldingDTO(
                        holding.getUser().getId(),
                        holding.getAsset(),
                        holding.getQuantity(),
                        holding.getAverageCost(),
                        holding.getRealizedProfitLoss()))
                .collect(Collectors.toList());


        return ResponseEntity.ok(holdings);
    }
}
