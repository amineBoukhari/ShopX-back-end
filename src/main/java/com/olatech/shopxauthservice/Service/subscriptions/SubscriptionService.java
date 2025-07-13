package com.olatech.shopxauthservice.Service.subscriptions;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.subscriptions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service gérant les abonnements des stores
 */
public interface SubscriptionService {

    /**
     * Crée un nouvel abonnement avec période d'essai
     * @param store Le store concerné
     * @param plan Le plan d'abonnement
     * @param billingCycle Le cycle de facturation (mensuel ou annuel)
     * @return L'abonnement créé
     */
    StoreSubscription createTrialSubscription(Store store, SubscriptionPlan plan, BillingCycle billingCycle);

    /**
     * Crée un nouvel abonnement avec période d'essai en utilisant l'ID du store
     * @param storeId ID du store concerné
     * @param plan Le plan d'abonnement
     * @param billingCycle Le cycle de facturation (mensuel ou annuel)
     * @return L'abonnement créé
     */
    StoreSubscription createTrialSubscriptionByStoreId(Long storeId, SubscriptionPlan plan, BillingCycle billingCycle);

    /**
     * Crée un abonnement sans période d'essai
     * @param store Le store concerné
     * @param plan Le plan d'abonnement
     * @param billingCycle Le cycle de facturation
     * @return L'abonnement créé
     */
    StoreSubscription createSubscription(Store store, SubscriptionPlan plan, BillingCycle billingCycle);

    /**
     * Trouve l'abonnement actif d'un store
     * @param store Le store
     * @return L'abonnement actif s'il existe
     */
    Optional<StoreSubscription> findActiveSubscription(Store store);

    /**
     * Trouve l'abonnement actif d'un store par son ID
     * @param storeId ID du store
     * @return L'abonnement actif s'il existe
     */
    Optional<StoreSubscription> findActiveSubscriptionByStoreId(Long storeId);

    /**
     * Récupère tous les abonnements d'un store
     * @param store Le store
     * @return Liste des abonnements
     */
    List<StoreSubscription> findAllSubscriptions(Store store);

    /**
     * Récupère tous les abonnements d'un store par son ID
     * @param storeId ID du store
     * @return Liste des abonnements
     */
    List<StoreSubscription> findAllSubscriptionsByStoreId(Long storeId);

    /**
     * Change le plan d'abonnement
     * @param subscription L'abonnement actuel
     * @param newPlan Le nouveau plan
     * @param billingCycle Le nouveau cycle de facturation
     * @return L'abonnement mis à jour
     */
    StoreSubscription changePlan(StoreSubscription subscription, SubscriptionPlan newPlan, BillingCycle billingCycle);

    /**
     * Met à jour un abonnement existant
     * @param subscription L'abonnement à mettre à jour
     * @return L'abonnement mis à jour
     */
    StoreSubscription updateSubscription(StoreSubscription subscription);

    /**
     * Renouvelle un abonnement
     * @param subscription L'abonnement à renouveler
     * @return L'abonnement renouvelé
     */
    StoreSubscription renewSubscription(StoreSubscription subscription);

    /**
     * Annule un abonnement
     * @param subscription L'abonnement à annuler
     * @param effectiveDate Date effective d'annulation (immédiate ou fin de période)
     * @return L'abonnement annulé
     */
    StoreSubscription cancelSubscription(StoreSubscription subscription, LocalDateTime effectiveDate);

    /**
     * Vérifie si un store peut ajouter un produit selon son plan
     * @param store Le store
     * @return true si le store peut ajouter un produit, false sinon
     */
    boolean canAddProduct(Store store);

    /**
     * Vérifie si un store peut ajouter un certain nombre de produits
     * @param store Le store
     * @param count Nombre de produits à ajouter
     * @return true si le store peut ajouter ce nombre de produits, false sinon
     */
    boolean canAddProducts(Store store, int count);

    /**
     * Met à jour le compteur de produits d'un store
     * @param store Le store
     * @param count Nombre actuel de produits
     */
    void updateProductCount(Store store, int count);

    /**
     * Trouve les abonnements qui arrivent à expiration bientôt
     * @param daysThreshold Nombre de jours avant expiration
     * @return Liste des abonnements qui expirent bientôt
     */
    List<StoreSubscription> findExpiringSubscriptions(int daysThreshold);

    /**
     * Traite les renouvellements automatiques
     * @return Nombre d'abonnements renouvelés
     */
    int processAutoRenewals();

    /**
     * Génère une facture pour un abonnement
     * @param subscription L'abonnement
     * @return La facture générée
     */
    SubscriptionInvoice generateInvoice(StoreSubscription subscription);

    /**
     * Récupère un store par son ID
     * @param storeId ID du store
     * @return Le store s'il existe
     */
    Optional<Store> getStoreById(Long storeId);
}
