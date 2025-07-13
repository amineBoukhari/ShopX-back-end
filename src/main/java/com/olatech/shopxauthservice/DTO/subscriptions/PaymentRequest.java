package com.olatech.shopxauthservice.DTO.subscriptions;

import lombok.Data;

@Data
public class PaymentRequest {
    private String paymentMethod;
    private String transactionId;
}
