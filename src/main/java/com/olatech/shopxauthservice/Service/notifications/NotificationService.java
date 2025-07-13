package com.olatech.shopxauthservice.Service.notifications;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionInvoice;
import com.olatech.shopxauthservice.Model.subscriptions.StoreSubscription;

/**
 * Service de gestion des notifications liées aux abonnements
 */
public interface NotificationService {

    /**
     * Envoie une notification de bienvenue pour un nouvel abonnement
     * @param user L'utilisateur destinataire
     * @param subscription L'abonnement concerné
     * @return true si la notification a été envoyée avec succès, false sinon
     */
    boolean sendWelcomeNotification(Users user, StoreSubscription subscription);

    /**
     * Envoie un rappel de fin de période d'essai
     * @param subscription L'abonnement en période d'essai
     * @param daysRemaining Jours restants avant la fin de l'essai
     * @return true si la notification a été envoyée avec succès, false sinon
     */
    boolean sendTrialEndingReminder(StoreSubscription subscription, int daysRemaining);

    /**
     * Envoie un rappel de renouvellement d'abonnement imminent
     * @param subscription L'abonnement concerné
     * @param daysRemaining Jours restants avant le renouvellement
     * @return true si la notification a été envoyée avec succès, false sinon
     */
    boolean sendRenewalReminder(StoreSubscription subscription, int daysRemaining);

    /**
     * Envoie une confirmation de paiement
     * @param invoice La facture payée
     * @return true si la notification a été envoyée avec succès, false sinon
     */
    boolean sendPaymentConfirmation(SubscriptionInvoice invoice);

    /**
     * Envoie une notification de facture impayée
     * @param invoice La facture en retard
     * @param daysOverdue Jours de retard
     * @return true si la notification a été envoyée avec succès, false sinon
     */
    boolean sendOverdueInvoiceNotification(SubscriptionInvoice invoice, int daysOverdue);

    /**
     * Envoie une notification d'expiration d'abonnement
     * @param subscription L'abonnement expiré
     * @return true si la notification a été envoyée avec succès, false sinon
     */
    boolean sendSubscriptionExpiredNotification(StoreSubscription subscription);

    /**
     * Envoie une notification d'approche des limites du plan
     * @param store Le store concerné
     * @param metricName Nom de la métrique (ex: "produits")
     * @param currentUsage Utilisation actuelle
     * @param maxAllowed Maximum autorisé
     * @param percentageUsed Pourcentage utilisé
     * @return true si la notification a été envoyée avec succès, false sinon
     */
    boolean sendLimitApproachingNotification(Store store, String metricName, 
                                            int currentUsage, int maxAllowed, 
                                            double percentageUsed);

    /**
     * Envoie une notification de changement de plan
     * @param subscription L'abonnement qui a changé de plan
     * @param oldPlanName Nom de l'ancien plan
     * @return true si la notification a été envoyée avec succès, false sinon
     */
    boolean sendPlanChangedNotification(StoreSubscription subscription, String oldPlanName);

    /**
     * Envoie une notification d'annulation d'abonnement
     * @param subscription L'abonnement annulé
     * @param effectiveDate Date effective d'annulation
     * @return true si la notification a été envoyée avec succès, false sinon
     */
    boolean sendCancellationNotification(StoreSubscription subscription, 
                                        java.time.LocalDateTime effectiveDate);
}
