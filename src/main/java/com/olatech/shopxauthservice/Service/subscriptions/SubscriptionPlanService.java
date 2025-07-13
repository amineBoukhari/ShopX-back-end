package com.olatech.shopxauthservice.Service.subscriptions;

import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionPlan;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service dédié à la gestion des plans d'abonnement
 */
public interface SubscriptionPlanService {

    /**
     * Crée un nouveau plan d'abonnement
     * @param name Nom du plan
     * @param description Description du plan
     * @param monthlyPrice Prix mensuel
     * @param yearlyPrice Prix annuel
     * @param maxProducts Nombre maximum de produits
     * @param trialPeriodDays Durée de la période d'essai en jours
     * @return Le plan créé
     */
    SubscriptionPlan createPlan(String name, String description, BigDecimal monthlyPrice, 
                              BigDecimal yearlyPrice, int maxProducts, Integer trialPeriodDays);

    /**
     * Met à jour un plan d'abonnement existant
     * @param planId ID du plan
     * @param name Nouveau nom
     * @param description Nouvelle description
     * @param monthlyPrice Nouveau prix mensuel
     * @param yearlyPrice Nouveau prix annuel
     * @param maxProducts Nouveau nombre maximum de produits
     * @param trialPeriodDays Nouvelle durée de période d'essai
     * @return Le plan mis à jour
     */
    SubscriptionPlan updatePlan(Long planId, String name, String description, BigDecimal monthlyPrice, 
                              BigDecimal yearlyPrice, int maxProducts, Integer trialPeriodDays);

    /**
     * Récupère un plan par son ID
     * @param planId ID du plan
     * @return Le plan s'il existe
     */
    Optional<SubscriptionPlan> findPlanById(Long planId);

    /**
     * Récupère un plan par son nom
     * @param name Nom du plan
     * @return Le plan s'il existe
     */
    Optional<SubscriptionPlan> findPlanByName(String name);

    /**
     * Récupère tous les plans d'abonnement actifs
     * @return Liste des plans actifs
     */
    List<SubscriptionPlan> findAllActivePlans();

    /**
     * Récupère tous les plans d'abonnement (actifs et inactifs)
     * @return Liste de tous les plans
     */
    List<SubscriptionPlan> findAllPlans();

    /**
     * Active ou désactive un plan
     * @param planId ID du plan
     * @param active Statut d'activation
     * @return Le plan mis à jour
     */
    SubscriptionPlan setActive(Long planId, boolean active);

    /**
     * Ajoute une fonctionnalité à un plan
     * @param planId ID du plan
     * @param feature Nom de la fonctionnalité
     * @return Le plan mis à jour
     */
    SubscriptionPlan addFeature(Long planId, String feature);

    /**
     * Retire une fonctionnalité d'un plan
     * @param planId ID du plan
     * @param feature Nom de la fonctionnalité
     * @return Le plan mis à jour
     */
    SubscriptionPlan removeFeature(Long planId, String feature);
}
