package com.olatech.shopxauthservice.DTO.subscriptions;

import com.olatech.shopxauthservice.Model.subscriptions.BillingCycle;
import lombok.Data;

@Data
public class SubscriptionRequest {
    private Long planId;
    private BillingCycle billingCycle;
}
