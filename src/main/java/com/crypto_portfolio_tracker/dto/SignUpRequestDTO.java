package com.crypto_portfolio_tracker.dto;

import lombok.Data;

@Data
public class SignUpRequestDTO {
    private String name;
    private String email;
    private String password;

}
