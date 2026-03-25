package com.crypto_portfolio_tracker.dto;

import com.crypto_portfolio_tracker.model.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenOrder {
    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("orderId")
    private Long orderId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("side")
    private String side;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("origQty")
    private BigDecimal origQty;

    @JsonProperty("executedQty")
    private BigDecimal executedQty;

    @JsonProperty("type")
    private String type;

    @JsonProperty("transactTime")
    private Long transactTime;

    @JsonProperty("fills")
    private List<FillDTO> fills;  //

    @JsonProperty("user")
    private Long userId;


}