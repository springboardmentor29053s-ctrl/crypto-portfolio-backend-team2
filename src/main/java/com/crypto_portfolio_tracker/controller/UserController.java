package com.crypto_portfolio_tracker.controller;

import com.crypto_portfolio_tracker.dto.LoginRequestDTO;
import com.crypto_portfolio_tracker.dto.SignUpRequestDTO;
import com.crypto_portfolio_tracker.service.SignUpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final SignUpService signUpService;

    public UserController(SignUpService signUpService) {

        this.signUpService = signUpService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequestDTO request) {
        return signUpService.signUp(request);
    }
}
