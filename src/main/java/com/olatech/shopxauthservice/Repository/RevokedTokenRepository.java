package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

/**
 * Repository pour gérer les opérations sur les tokens révoqués.
 */
@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    /**
     * Vérifie si un token est révoqué par son identifiant unique.
     * 
     * @param tokenId L'identifiant du token (jti)
     * @return Le token révoqué s'il existe
     */
    Optional<RevokedToken> findByTokenId(String tokenId);

    /**
     * Supprime les tokens révoqués qui sont expirés.
     * 
     * @param expiryDate Date d'expiration de référence
     * @return Le nombre de tokens supprimés
     */
    @Modifying
    @Query("DELETE FROM RevokedToken rt WHERE rt.expiryDate < ?1")
    int deleteExpiredTokens(Date expiryDate);
}
