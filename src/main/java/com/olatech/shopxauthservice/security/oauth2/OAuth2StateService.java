package com.olatech.shopxauthservice.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service responsable de la gestion du paramètre "state" dans le flux OAuth2
 * pour prévenir les attaques CSRF.
 */
@Service
public class OAuth2StateService {

    private static final String STATE_PARAMETER_KEY = "oauth2_state";
    private static final String PKCE_CODE_VERIFIER_KEY = "pkce_code_verifier";
    private static final String REDIRECT_URI_KEY = "oauth2_redirect_uri";
    private static final long STATE_EXPIRATION_MS = 600000; // 10 minutes

    /**
     * Génère un nouveau paramètre "state" aléatoire et le stocke en session.
     * 
     * @param request La requête HTTP actuelle
     * @return Le paramètre state généré
     */
    public String generateAndStoreState(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String state = generateRandomState();
        
        Map<String, Object> stateData = new HashMap<>();
        stateData.put("value", state);
        stateData.put("expiresAt", System.currentTimeMillis() + STATE_EXPIRATION_MS);
        
        session.setAttribute(STATE_PARAMETER_KEY, stateData);
        return state;
    }

    /**
     * Génère un code verifier PKCE et le stocke en session.
     * 
     * @param request La requête HTTP actuelle
     * @return Le code verifier généré
     */
    public String generateAndStorePkceCodeVerifier(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String codeVerifier = generatePkceCodeVerifier();
        session.setAttribute(PKCE_CODE_VERIFIER_KEY, codeVerifier);
        return codeVerifier;
    }

    /**
     * Récupère le code verifier PKCE stocké en session.
     * 
     * @param request La requête HTTP actuelle
     * @return Le code verifier ou null s'il n'existe pas
     */
    public String retrievePkceCodeVerifier(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        String codeVerifier = (String) session.getAttribute(PKCE_CODE_VERIFIER_KEY);
        // Suppression après utilisation
        if (codeVerifier != null) {
            session.removeAttribute(PKCE_CODE_VERIFIER_KEY);
        }
        
        return codeVerifier;
    }
    
    /**
     * Stores the redirect URI to use after successful authentication.
     * 
     * @param request The HTTP request
     * @param redirectUri The redirect URI to store
     */
    public void storeRedirectUri(HttpServletRequest request, String redirectUri) {
        HttpSession session = request.getSession(true);
        session.setAttribute(REDIRECT_URI_KEY, redirectUri);
    }

    /**
     * Retrieves the stored redirect URI.
     * 
     * @param request The HTTP request
     * @return The stored redirect URI or null if not found
     */
    public String retrieveRedirectUri(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        String redirectUri = (String) session.getAttribute(REDIRECT_URI_KEY);
        // Remove after retrieval
        if (redirectUri != null) {
            session.removeAttribute(REDIRECT_URI_KEY);
        }
        
        return redirectUri;
    }

    /**
     * Vérifie que le paramètre state reçu correspond à celui stocké en session
     * et qu'il n'est pas expiré.
     * 
     * @param request La requête HTTP
     * @param stateParam Le paramètre state reçu du fournisseur OAuth2
     * @return true si le state est valide, false sinon
     */
    public boolean validateState(HttpServletRequest request, String stateParam) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> stateData = (Map<String, Object>) session.getAttribute(STATE_PARAMETER_KEY);
        if (stateData == null) {
            return false;
        }
        
        // Suppression après validation
        session.removeAttribute(STATE_PARAMETER_KEY);
        
        String storedState = (String) stateData.get("value");
        long expiresAt = (long) stateData.get("expiresAt");
        
        return storedState != null && 
               storedState.equals(stateParam) && 
               System.currentTimeMillis() < expiresAt;
    }

    /**
     * Génère un paramètre state aléatoire en utilisant UUID.
     */
    private String generateRandomState() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Génère un code verifier PKCE aléatoire.
     */
    private String generatePkceCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] verifier = new byte[32];
        secureRandom.nextBytes(verifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(verifier);
    }
}
