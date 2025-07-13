package com.olatech.shopxauthservice.utils;

import com.olatech.shopxauthservice.Service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Outil utilitaire pour traiter les tokens JWT, y compris ceux qui sont expirés
 */
@Slf4j
@Component
public class JwtExpirationTool {
    
    private static final String DEFAULT_SECRET = "YmFzZTY0IGVuY29kaW5fdfdsfkjdhsfuidksghfilsdufgsdejhfgsdhjkfgsdkjufgoiusdyfguoseydfgeuszyfgezfbnzeuiofrnyzeuiofryzeyiurftbzevuyfrezuytdfrtezivuydfzefueznghfiuezynbfzebtsebfiuezy";
    private static final String FALLBACK_SECRET = "legrosculdetamereugfyudgghhjklgkfcxhuyguhjbgchfhjbvgcfdjtyguhjgjcfhjkvgcfdtjygkuhjvcfhtyghjvgcfjyhjkbvgchf";
    
    @Autowired
    private JWTService jwtService;
    
    /**
     * Tente d'extraire l'identifiant JWT (jti) même si le token est expiré
     * 
     * @param token Le token JWT à analyser
     * @return L'identifiant du token ou null si impossible à extraire
     */
    public String extractTokenIdEvenIfExpired(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        try {
            // Essayer d'abord avec le service JWT normal
            return jwtService.extractTokenId(token);
        } catch (ExpiredJwtException e) {
            // Le token est expiré mais on peut quand même extraire les claims
            try {
                return e.getClaims().get("jti", String.class);
            } catch (Exception ex) {
                log.warn("Impossible d'extraire le jti du token expiré", ex);
            }
        } catch (Exception e) {
            log.warn("Erreur lors de l'extraction du jti du token", e);
        }
        
        // Essayer manuellement avec différentes clés
        return tryExtractTokenIdWithVariousKeys(token);
    }
    
    /**
     * Essaie d'extraire l'ID du token en utilisant différentes clés
     */
    private String tryExtractTokenIdWithVariousKeys(String token) {
        // Liste des clés possibles à essayer
        String[] possibleSecrets = {DEFAULT_SECRET, FALLBACK_SECRET};
        
        for (String secret : possibleSecrets) {
            try {
                // Générer une clé à partir du secret
                SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
                
                // Parser le token sans vérifier la signature ni l'expiration
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .clockSkewSeconds(Integer.MAX_VALUE) // Ignorer l'expiration
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                
                String jti = claims.get("jti", String.class);
                if (jti != null) {
                    log.info("ID de token extrait avec succès en utilisant une clé alternative");
                    return jti;
                }
            } catch (Exception e) {
                // Continuer à essayer avec la prochaine clé
            }
        }
        
        return null;
    }
    
    /**
     * Vérifie si un token est expiré, même s'il a des problèmes de signature
     */
    public boolean isTokenExpiredIgnoringSignature(String token) {
        if (token == null || token.isEmpty()) {
            return true;
        }
        
        try {
            Date expiration = jwtService.extractExpiration(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            // Par définition, c'est expiré
            return true;
        } catch (Exception e) {
            // Pour les erreurs de signature, essayer avec différentes clés
            String[] possibleSecrets = {DEFAULT_SECRET, FALLBACK_SECRET};
            
            for (String secret : possibleSecrets) {
                try {
                    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
                    
                    // Parser sans vérifier la signature
                    Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
                    
                    Date expiration = claims.getExpiration();
                    return expiration.before(new Date());
                } catch (ExpiredJwtException ex) {
                    return true;
                } catch (Exception ex) {
                    // Continuer avec la prochaine clé
                }
            }
            
            // Si aucune clé ne fonctionne, considérer le token comme expiré par défaut
            return true;
        }
    }
}
