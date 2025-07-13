package com.olatech.shopxauthservice.Service.analytics;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.subscriptions.BillingCycle;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Service d'analyse des métriques d'abonnement et d'utilisation
 */
public interface AnalyticsService {

    /**
     * Calcule le taux de conversion des essais gratuits en abonnements payants
     * @param startDate Date de début de la période d'analyse
     * @param endDate Date de fin de la période d'analyse
     * @return Taux de conversion (0.0 à 1.0)
     */
    double getTrialConversionRate(LocalDate startDate, LocalDate endDate);

    /**
     * Calcule le revenu mensuel récurrent (MRR)
     * @param date Date pour laquelle calculer le MRR
     * @return Montant du MRR
     */
    BigDecimal getMonthlyRecurringRevenue(LocalDate date);

    /**
     * Calcule le taux de rétention des abonnements
     * @param period Période d'analyse (en mois)
     * @return Taux de rétention (0.0 à 1.0)
     */
    double getRetentionRate(int period);

    /**
     * Calcule le taux d'attrition (churn rate) des abonnements
     * @param period Période d'analyse (en mois)
     * @return Taux d'attrition (0.0 à 1.0)
     */
    double getChurnRate(int period);

    /**
     * Trouve les stores qui approchent de leur limite de produits
     * @param thresholdPercentage Pourcentage seuil (ex: 0.8 pour 80%)
     * @return Liste des stores avec leur utilisation
     */
    List<StoreUsageDTO> findStoresNearProductLimit(double thresholdPercentage);

    /**
     * Génère un rapport mensuel des revenus
     * @param yearMonth Année et mois du rapport
     * @return Rapport de revenus
     */
    RevenueReportDTO generateMonthlyRevenueReport(YearMonth yearMonth);

    /**
     * Calcule la distribution des abonnements par plan
     * @return Map avec le plan comme clé et le nombre d'abonnements comme valeur
     */
    Map<SubscriptionPlan, Integer> getSubscriptionDistributionByPlan();

    /**
     * Calcule la distribution des abonnements par cycle de facturation
     * @return Map avec le cycle comme clé et le nombre d'abonnements comme valeur
     */
    Map<BillingCycle, Integer> getSubscriptionDistributionByBillingCycle();

    /**
     * Calcule l'utilisation moyenne des produits par plan
     * @return Map avec le plan comme clé et le pourcentage moyen d'utilisation comme valeur
     */
    Map<SubscriptionPlan, Double> getAverageProductUsageByPlan();

    /**
     * Calcule le taux de renouvellement automatique
     * @return Taux de renouvellement (0.0 à 1.0)
     */
    double getAutoRenewalRate();

    /**
     * Calcule la valeur à vie moyenne des clients (LTV)
     * @return Valeur moyenne à vie
     */
    BigDecimal getAverageCustomerLifetimeValue();

    /**
     * Met à jour les métriques d'utilisation pour un store spécifique
     * @param store Le store
     * @return true si la mise à jour a réussi, false sinon
     */
    boolean updateUsageMetrics(Store store);

    /**
     * Génère un rapport de performance des abonnements
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Rapport de performance
     */
    SubscriptionPerformanceReportDTO generatePerformanceReport(LocalDate startDate, LocalDate endDate);
}
