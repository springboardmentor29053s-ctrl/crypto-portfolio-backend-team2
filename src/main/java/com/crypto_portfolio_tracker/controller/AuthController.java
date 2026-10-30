package com.crypto_portfolio_tracker.controller;

import com.crypto_portfolio_tracker.config.JwtUtil;
import com.crypto_portfolio_tracker.dto.LoginRequestDTO;
import com.crypto_portfolio_tracker.service.PasswordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordService passwordService;

    public AuthController(AuthenticationManager authenticationManager,
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

//     //Endpoint for changing password
//     @PostMapping("/forgot-password")
//     public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
//            String email = request.getEmail();
//        log.debug("Forgot Password Request, {}", email);
//         return new ResponseEntity<>(passwordService.createResetToken(email), null, 200);
//     }

//    //  2. Reset Password
//    @PostMapping("/reset-password")
//    public String resetPassword(
//            @RequestParam String token,
//            @RequestParam String newPassword) {
//
//        passwordService.resetPassword(token, newPassword);
//        log.info("Password reset Success , {}", newPassword);
//        return "Password reset successful";
//    }
}
