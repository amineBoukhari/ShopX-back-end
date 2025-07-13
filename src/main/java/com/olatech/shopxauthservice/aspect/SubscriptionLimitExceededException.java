package com.olatech.shopxauthservice.aspect;

import lombok.Getter;

/**
 * Exception levée lorsqu'une opération dépasse les limites
 * définies par le plan d'abonnement.
 */
@Getter
public class SubscriptionLimitExceededException extends RuntimeException {

    private final LimitType limitType;
    private final int currentValue;
    private final int maxAllowed;

    public SubscriptionLimitExceededException(String message, LimitType limitType, int currentValue, int maxAllowed) {
        super(message);
        this.limitType = limitType;
        this.currentValue = currentValue;
        this.maxAllowed = maxAllowed;
    }

    public SubscriptionLimitExceededException(String message, LimitType limitType) {
        super(message);
        this.limitType = limitType;
        this.currentValue = 0;
        this.maxAllowed = 0;
    }

    @Override
    public String toString() {
        return "SubscriptionLimitExceededException: " + getMessage() + 
               " [limitType=" + limitType + 
               ", currentValue=" + currentValue + 
               ", maxAllowed=" + maxAllowed + "]";
    }
}
