package com.olatech.shopxauthservice.Service.subscriptions;

import com.olatech.shopxauthservice.Model.subscriptions.InvoiceStatus;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionInvoice;
import com.olatech.shopxauthservice.Model.subscriptions.StoreSubscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service dédié à la gestion des factures d'abonnement
 */
public interface SubscriptionInvoiceService {

    /**
     * Génère une nouvelle facture pour un abonnement
     * @param subscription L'abonnement concerné
     * @param amount Montant de la facture
     * @param dueDate Date d'échéance
     * @return La facture générée
     */
    SubscriptionInvoice generateInvoice(StoreSubscription subscription, BigDecimal amount, LocalDateTime dueDate);

    /**
     * Récupère une facture par son ID
     * @param invoiceId ID de la facture
     * @return La facture si elle existe
     */
    Optional<SubscriptionInvoice> findInvoiceById(Long invoiceId);

    /**
     * Récupère toutes les factures d'un abonnement
     * @param subscriptionId ID de l'abonnement
     * @return Liste des factures
     */
    List<SubscriptionInvoice> findInvoicesBySubscriptionId(Long subscriptionId);

    /**
     * Récupère toutes les factures d'un store
     * @param storeId ID du store
     * @return Liste des factures
     */
    List<SubscriptionInvoice> findInvoicesByStoreId(Long storeId);

    /**
     * Marque une facture comme payée
     * @param invoiceId ID de la facture
     * @param paymentMethod Méthode de paiement utilisée
     * @param paidDate Date de paiement
     * @return La facture mise à jour
     */
    SubscriptionInvoice markAsPaid(Long invoiceId, String paymentMethod, LocalDateTime paidDate);

    /**
     * Met à jour le statut d'une facture
     * @param invoiceId ID de la facture
     * @param status Nouveau statut
     * @return La facture mise à jour
     */
    SubscriptionInvoice updateStatus(Long invoiceId, InvoiceStatus status);

    /**
     * Annule une facture
     * @param invoiceId ID de la facture
     * @param reason Motif d'annulation
     * @return La facture annulée
     */
    SubscriptionInvoice cancelInvoice(Long invoiceId, String reason);

    /**
     * Récupère les factures en retard de paiement
     * @param days Nombre de jours de retard minimum
     * @return Liste des factures en retard
     */
    List<SubscriptionInvoice> findOverdueInvoices(int days);

    /**
     * Génère une facture pour le renouvellement d'un abonnement
     * @param subscription L'abonnement à renouveler
     * @return La facture générée
     */
    SubscriptionInvoice generateRenewalInvoice(StoreSubscription subscription);

    /**
     * Génère un numéro de facture unique
     * @return Numéro de facture
     */
    String generateInvoiceNumber();

    /**
     * Calcule le montant total des factures sur une période
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Montant total
     */
    BigDecimal calculateTotalInvoiceAmount(LocalDateTime startDate, LocalDateTime endDate);
}
