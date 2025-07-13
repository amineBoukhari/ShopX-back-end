package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.Model.StoreRole;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Repository.StoreRoleRepository;
import com.olatech.shopxauthservice.Service.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JWTService {

    // Secret fixe pour assurer la compatibilité avec les tokens existants
    private static final String DEFAULT_SECRET = "legrosculdetamereugfyudgghhjklgkfcxhuyguhjbgchfhjbvgcfdjtyguhjgjcfhjkvgcfdtjygkuhjvcfhtyghjvgcfjyhjkbvgchf";
    
    @Value("${jwt.secret:legrosculdetamereugfyudgghhjklgkfcxhuyguhjbgchfhjbvgcfdjtyguhjgjcfhjkvgcfdtjygkuhjvcfhtyghjvgcfjyhjkbvgchf}")
    private String configuredSecret;
    
    @Value("${jwt.access-token.expiration:360000000}")
    private long accessTokenExpiration; // 1 heure par défaut
    
    @Value("${jwt.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration; // 7 jours par défaut
    @Autowired
    private StoreRoleRepository storeRoleRepository;
    
    @Autowired
    @Lazy
    private AuthorizationService authorizationService;
    public String generateToken(Users user) {
        // Génération d'un identifiant unique pour le token
        String tokenId = UUID.randomUUID().toString();
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("jti", tokenId); // JWT ID unique pour révocation

        List<StoreRole> storeRoles = storeRoleRepository.findByUser(user);
        // store access should be an Object with storeId and access level
        List<Map<String, Object>> storeAccess = storeRoles.stream().map(storeRole -> {
            Map<String, Object> store = new HashMap<>();
            store.put("storeId", storeRole.getStore().getId());
            store.put("role", storeRole.getRole());
            return store;
        }).collect(Collectors.toList());

        claims.put("stores", storeAccess);
        
        // Add detailed permissions
        claims.put("permissions", authorizationService.getUserPermissions(user));
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration * 1000);
        
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .and()
                .signWith(getKey())
                .compact();

    }
    public String generateRefreshToken(String username) {
        // Génération d'un identifiant unique pour le token
        String tokenId = UUID.randomUUID().toString();
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("jti", tokenId); // JWT ID unique pour révocation
        claims.put("type", "refresh"); // Type de token
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration * 1000);
        
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .and()
                .signWith(getKey())
                .compact();

    }

    public String generateTokenWithClaims(Map<String, Object> claims, long expirationTime) {
        return Jwts.builder()
                .claims()
                .add(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .and()
                .signWith(getKey())
                .compact();
    }

    public Long extractInvitationId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("invitationId", Long.class);
    }

    public SecretKey getKey() {
        try {
            // Déterminer quelle clé utiliser
            String secretToUse = configuredSecret;
            
            // Si la clé configurée est vide, utiliser la clé par défaut
            if (secretToUse == null || secretToUse.isEmpty()) {
                log.warn("La clé secrète configurée est vide, utilisation de la clé par défaut");
                secretToUse = DEFAULT_SECRET;
            }
            
            // Générer la clé secrète
            try {
                byte[] keyBytes = secretToUse.getBytes();
                return Keys.hmacShaKeyFor(keyBytes);
            } catch (Exception e) {
                log.error("Erreur lors de la génération de la clé JWT", e);
                // Fallback en dernier recours
                return Keys.hmacShaKeyFor(DEFAULT_SECRET.getBytes());
            }
        } catch (Exception e) {
            log.error("Erreur grave lors de la génération de la clé JWT", e);
            throw new RuntimeException("Impossible de générer la clé JWT", e);
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        if (token == null) {
            return false;
        }
        
        try {
            final String username = extractUserName(token);
            final String tokenId = extractTokenId(token);
            
            // Vérifier si le token a été révoqué (à implémenter)
            // if (tokenRevocationService.isTokenRevoked(tokenId)) {
            //    return false;
            // }
            
            return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUserName(String token) {
        // extract the username from jwt token
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    /**
     * Extrait l'identifiant unique du token (jti).
     *
     * @param token Le token JWT
     * @return L'identifiant du token ou null si non présent
     */
    public String extractTokenId(String token) {
        try {
            return extractClaim(token, claims -> claims.get("jti", String.class));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Vérifie si un token est un token de rafraîchissement.
     *
     * @param token Le token JWT
     * @return true si c'est un token de rafraîchissement, false sinon
     */
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = extractClaim(token, claims -> claims.get("type", String.class));
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
}
