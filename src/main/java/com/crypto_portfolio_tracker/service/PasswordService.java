package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.model.PasswordResetToken;
import com.crypto_portfolio_tracker.model.UserEntity;
import com.crypto_portfolio_tracker.repository.PasswordResetTokenRepository;
import com.crypto_portfolio_tracker.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PasswordService {
    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;

    public PasswordService(UserRepository userRepo,
                           PasswordResetTokenRepository tokenRepo,
                           PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.passwordEncoder = passwordEncoder;
    }

    //  STEP 1: Generate Token
    @Transactional
    public String createResetToken(String email) {

        UserEntity user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Email not registered"));

        //  DELETE OLD TOKENS
        tokenRepo.deleteByUser(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        token.setUsed(false);
        tokenRepo.save(token);
        log.info("EMAIL RECEIVED = {}", email);
        return token.getToken();

    }

    //  STEP 2: Reset Password
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Token already used");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        UserEntity user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        resetToken.setUsed(true);
        tokenRepo.save(resetToken);
    }
}
