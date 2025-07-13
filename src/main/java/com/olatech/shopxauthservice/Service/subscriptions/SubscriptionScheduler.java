package com.olatech.shopxauthservice.Service.subscriptions;

import com.olatech.shopxauthservice.Model.subscriptions.StoreSubscription;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionInvoice;

import java.util.List;

/**
 * Service planificateur pour gérer les tâches automatiques liées aux abonnements
 */
public interface SubscriptionScheduler {

    /**
     * Traite les renouvellements automatiques d'abonnements
     * @return Nombre d'abonnements renouvelés
     */
    int processAutoRenewals();

    /**
     * Traite les expirations d'abonnements
     * @return Nombre d'abonnements expirés
     */
    int processExpirations();

    /**
     * Envoie des rappels pour les abonnements qui arrivent à expiration
     * @param daysThreshold Nombre de jours avant expiration
     * @return Nombre de rappels envoyés
     */
    int sendExpirationReminders(int daysThreshold);

    /**
     * Envoie des rappels pour les périodes d'essai qui se terminent
     * @param daysThreshold Nombre de jours avant la fin de l'essai
     * @return Nombre de rappels envoyés
     */
    int sendTrialEndingReminders(int daysThreshold);

    /**
     * Envoie des rappels pour les factures impayées
     * @param daysOverdue Nombre de jours de retard
     * @return Nombre de rappels envoyés
     */
    int sendOverdueInvoiceReminders(int daysOverdue);

    /**
     * Génère les factures de renouvellement pour les abonnements proches de leur date de renouvellement
     * @param daysThreshold Nombre de jours avant le renouvellement
     * @return Liste des factures générées
     */
    List<SubscriptionInvoice> generateRenewalInvoices(int daysThreshold);

    /**
     * Vérifie et met à jour les statuts des abonnements
     * @return Nombre d'abonnements mis à jour
     */
    int updateSubscriptionStatuses();

    /**
     * Envoie des alertes pour les stores approchant leurs limites
     * @param thresholdPercentage Pourcentage seuil (ex: 0.8 pour 80%)
     * @return Nombre d'alertes envoyées
     */
    int sendLimitApproachingAlerts(double thresholdPercentage);

    /**
     * Annule les abonnements programmés pour annulation
     * @return Nombre d'abonnements annulés
     */
    int processPendingCancellations();

    /**
     * Met à jour les métriques d'utilisation pour tous les stores
     * @return Nombre de métriques mises à jour
     */
    int updateAllUsageMetrics();
}
