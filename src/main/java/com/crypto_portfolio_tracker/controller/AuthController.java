package com.crypto_portfolio_tracker.controller;

import com.crypto_portfolio_tracker.config.JwtUtil;
import com.crypto_portfolio_tracker.dto.ForgotPasswordRequest;
import com.crypto_portfolio_tracker.dto.LoginRequestDTO;
import com.crypto_portfolio_tracker.dto.ResetPasswordRequest;
import com.crypto_portfolio_tracker.service.CustomUserDetailsService;
import com.crypto_portfolio_tracker.service.PasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordService passwordService;

    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil, PasswordService passwordService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordService = passwordService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtUtil.generateToken(request.getEmail());
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", request.getEmail());

        return ResponseEntity.ok(response);
    }

    // Endpoint for changing password
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordService.forgotPassword(request);
        return ResponseEntity.ok("Password reset token sent to email if user exists.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean success = passwordService.resetPassword(request);
        if (success) return ResponseEntity.ok("Password reset successful.");
        return ResponseEntity.badRequest().body("Invalid or expired token.");
    }

}
