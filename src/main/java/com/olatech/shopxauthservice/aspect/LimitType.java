package com.olatech.shopxauthservice.aspect;

/**
 * Types de limites pouvant être vérifiées dans un plan d'abonnement
 */
public enum LimitType {
    PRODUCT,            // Limite du nombre de produits
    API_CALL,           // Limite du nombre d'appels API
    STORAGE,            // Limite d'utilisation du stockage
    BANDWIDTH,          // Limite de bande passante
    USER,               // Limite du nombre d'utilisateurs
    FEATURE             // Accès à une fonctionnalité spécifique
}
