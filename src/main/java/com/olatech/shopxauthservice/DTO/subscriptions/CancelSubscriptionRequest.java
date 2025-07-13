package com.olatech.shopxauthservice.DTO.subscriptions;

import lombok.Data;

@Data
public class CancelSubscriptionRequest {
    private boolean cancelImmediately;
    private String reason;
}
