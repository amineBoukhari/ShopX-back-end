package com.olatech.shopxauthservice.aspect;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global pour traiter les exceptions liées aux limites d'abonnement
 */
@ControllerAdvice
public class SubscriptionExceptionHandler {

    /**
     * Gère les exceptions de dépassement de limites d'abonnement
     */
    @ExceptionHandler(SubscriptionLimitExceededException.class)
    public ResponseEntity<Object> handleSubscriptionLimitExceeded(SubscriptionLimitExceededException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.PAYMENT_REQUIRED.value());
        body.put("error", "Subscription Limit Exceeded");
        body.put("message", ex.getMessage());
        body.put("limitType", ex.getLimitType());
        
        if (ex.getCurrentValue() > 0) {
            body.put("currentValue", ex.getCurrentValue());
            body.put("maxAllowed", ex.getMaxAllowed());
            body.put("usagePercentage", 
                    Math.round((double) ex.getCurrentValue() / ex.getMaxAllowed() * 100) + "%");
        }
        
        // Inclure des informations sur la mise à niveau
        body.put("solution", "Please upgrade your subscription plan to access this feature or increase your limits.");
        
        return new ResponseEntity<>(body, HttpStatus.PAYMENT_REQUIRED);
    }
}
