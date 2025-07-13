package com.olatech.shopxauthservice.DTO.subscriptions;

import com.olatech.shopxauthservice.Model.subscriptions.BillingCycle;
import lombok.Data;

@Data
public class ChangePlanRequest {
    private Long newPlanId;
    private BillingCycle billingCycle;
}
