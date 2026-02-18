package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.LoginRequestDTO;
import com.crypto_portfolio_tracker.dto.SignUpRequestDTO;
import org.springframework.http.ResponseEntity;


public interface SignUpService {

    //singUp logic
    ResponseEntity<String> signUp(SignUpRequestDTO request);
    ResponseEntity<String> login(LoginRequestDTO request);

}
