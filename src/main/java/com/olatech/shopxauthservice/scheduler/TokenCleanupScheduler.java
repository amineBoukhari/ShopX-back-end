package com.olatech.shopxauthservice.scheduler;

import com.olatech.shopxauthservice.Service.TokenRevocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Planificateur responsable du nettoyage périodique des tokens révoqués expirés
 */
@Component
public class TokenCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupScheduler.class);

    @Autowired
    private TokenRevocationService tokenRevocationService;

    /**
     * Nettoie les tokens révoqués expirés selon la configuration cron
     */
    @Scheduled(cron = "${cron.cleanup-expired-tokens:0 0 1 * * ?}")
    public void cleanupExpiredTokens() {
        logger.info("Démarrage du nettoyage planifié des tokens révoqués expirés");
        
        try {
            int deletedCount = tokenRevocationService.cleanupExpiredTokens();
            logger.info("Nettoyage des tokens révoqués terminé - {} tokens supprimés", deletedCount);
        } catch (Exception e) {
            logger.error("Erreur lors du nettoyage des tokens révoqués", e);
        }
    }
}
