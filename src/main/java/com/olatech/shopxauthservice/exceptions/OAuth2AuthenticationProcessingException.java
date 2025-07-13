package com.olatech.shopxauthservice.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception personnalisée pour les erreurs lors du traitement de l'authentification OAuth2.
 */
public class OAuth2AuthenticationProcessingException extends AuthenticationException {

    public OAuth2AuthenticationProcessingException(String msg) {
        super(msg);
    }

    public OAuth2AuthenticationProcessingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
