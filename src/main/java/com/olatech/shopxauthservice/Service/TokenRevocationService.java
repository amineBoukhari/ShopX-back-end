package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.Model.RevokedToken;
import com.olatech.shopxauthservice.Repository.RevokedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Service de gestion des tokens révoqués.
 */
@Service
public class TokenRevocationService {

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Autowired
    private JWTService jwtService;

    /**
     * Révoque un token d'accès par son identifiant.
     * 
     * @param tokenId Identifiant unique du token
     * @param expiryDate Date d'expiration du token
     * @param revokedBy Identifiant de l'utilisateur ayant révoqué le token
     */
    @Transactional
    public void revokeAccessToken(String tokenId, Date expiryDate, String revokedBy) {
        RevokedToken revokedToken = new RevokedToken(tokenId, expiryDate, "ACCESS");
        revokedToken.setRevokedBy(revokedBy);
        revokedTokenRepository.save(revokedToken);
    }

    /**
     * Révoque un token de rafraîchissement par son identifiant.
     * 
     * @param tokenId Identifiant unique du token
     * @param expiryDate Date d'expiration du token
     * @param revokedBy Identifiant de l'utilisateur ayant révoqué le token
     */
    @Transactional
    public void revokeRefreshToken(String tokenId, Date expiryDate, String revokedBy) {
        RevokedToken revokedToken = new RevokedToken(tokenId, expiryDate, "REFRESH");
        revokedToken.setRevokedBy(revokedBy);
        revokedTokenRepository.save(revokedToken);
    }

    /**
     * Révoque un token JWT en extrayant ses informations.
     * 
     * @param token Le token JWT à révoquer
     * @param tokenType Le type de token (ACCESS ou REFRESH)
     * @param revokedBy Identifiant de l'utilisateur ayant révoqué le token
     * @return true si le token a été révoqué avec succès, false sinon
     */
    @Transactional
    public boolean revokeToken(String token, String tokenType, String revokedBy) {
        try {
            String tokenId = jwtService.extractTokenId(token);
            Date expiryDate = jwtService.extractExpiration(token);
            
            if (tokenId == null || expiryDate == null) {
                return false;
            }
            
            RevokedToken revokedToken = new RevokedToken(tokenId, expiryDate, tokenType);
            revokedToken.setRevokedBy(revokedBy);
            revokedTokenRepository.save(revokedToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si un token est révoqué.
     * 
     * @param tokenId Identifiant unique du token
     * @return true si le token est révoqué, false sinon
     */
    @Transactional(readOnly = true)
    public boolean isTokenRevoked(String tokenId) {
        return revokedTokenRepository.findByTokenId(tokenId).isPresent();
    }

    /**
     * Nettoie les tokens révoqués expirés (exécuté quotidiennement à minuit).
     */
    /**
     * Nettoie les tokens révoqués qui sont expirés
     * 
     * @return Le nombre de tokens supprimés
     */
    @Transactional
    public int cleanupExpiredTokens() {
        return revokedTokenRepository.deleteExpiredTokens(new Date());
    }
}
