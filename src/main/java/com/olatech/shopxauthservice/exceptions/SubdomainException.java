package com.olatech.shopxauthservice.exceptions;

/**
 * Exception personnalisée pour les erreurs liées aux sous-domaines
 */
public class SubdomainException extends RuntimeException {
    
    private final String subdomain;
    
    public SubdomainException(String message, String subdomain) {
        super(message);
        this.subdomain = subdomain;
    }
    
    public String getSubdomain() {
        return subdomain;
    }
}
