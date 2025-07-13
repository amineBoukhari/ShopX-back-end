package com.olatech.shopxauthservice.Service.subscriptions;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.subscriptions.StoreSubscription;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionPlan;

/**
 * Service de validation des opérations liées aux abonnements
 */
public interface SubscriptionValidator {

    /**
     * Vérifie si un store peut ajouter un produit selon son plan
     * @param store Le store concerné
     * @return true si le store peut ajouter un produit, false sinon
     */
    boolean canAddProduct(Store store);

    /**
     * Vérifie si un store peut ajouter un certain nombre de produits
     * @param store Le store concerné
     * @param count Nombre de produits à ajouter
     * @return true si le store peut ajouter ces produits, false sinon
     */
    boolean canAddProducts(Store store, int count);

    /**
     * Vérifie si un store a accès à une fonctionnalité spécifique
     * @param store Le store concerné
     * @param featureName Nom de la fonctionnalité
     * @return true si le store a accès à la fonctionnalité, false sinon
     */
    boolean hasFeatureAccess(Store store, String featureName);

    /**
     * Vérifie si un abonnement est actif
     * @param subscription L'abonnement
     * @return true si l'abonnement est actif, false sinon
     */
    boolean isSubscriptionActive(StoreSubscription subscription);

    /**
     * Vérifie si un abonnement est en période d'essai
     * @param subscription L'abonnement
     * @return true si l'abonnement est en période d'essai, false sinon
     */
    boolean isInTrialPeriod(StoreSubscription subscription);

    /**
     * Vérifie si un abonnement est expiré
     * @param subscription L'abonnement
     * @return true si l'abonnement est expiré, false sinon
     */
    boolean isSubscriptionExpired(StoreSubscription subscription);

    /**
     * Vérifie si un changement de plan est valide
     * @param currentPlan Plan actuel
     * @param newPlan Nouveau plan
     * @return true si le changement est valide, false sinon
     */
    boolean isPlanChangeValid(SubscriptionPlan currentPlan, SubscriptionPlan newPlan);

    /**
     * Vérifie si un store est en situation de dépassement de limites
     * @param store Le store concerné
     * @return true si le store dépasse les limites de son plan, false sinon
     */
    boolean isOverLimit(Store store);

    /**
     * Calcule le nombre de jours restants avant l'expiration d'un abonnement
     * @param subscription L'abonnement
     * @return Nombre de jours restants
     */
    int daysUntilExpiration(StoreSubscription subscription);

    /**
     * Vérifie si un abonnement est éligible au renouvellement automatique
     * @param subscription L'abonnement
     * @return true si l'abonnement est éligible, false sinon
     */
    boolean isEligibleForAutoRenewal(StoreSubscription subscription);
}
