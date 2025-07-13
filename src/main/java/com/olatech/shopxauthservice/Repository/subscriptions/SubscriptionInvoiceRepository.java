package com.olatech.shopxauthservice.Repository.subscriptions;

import com.olatech.shopxauthservice.Model.subscriptions.InvoiceStatus;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionInvoice;
import com.olatech.shopxauthservice.Model.subscriptions.StoreSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionInvoiceRepository extends JpaRepository<SubscriptionInvoice, Long> {
    
    /**
     * Trouve les factures par abonnement
     */
    List<SubscriptionInvoice> findBySubscriptionOrderByIssuedDateDesc(StoreSubscription subscription);
    
    /**
     * Trouve les factures par abonnement ID
     */
    List<SubscriptionInvoice> findBySubscriptionIdOrderByIssuedDateDesc(Long subscriptionId);
    
    /**
     * Trouve les factures par statut
     */
    List<SubscriptionInvoice> findByStatus(InvoiceStatus status);
    
    /**
     * Trouve les factures impayées
     */
    @Query("SELECT i FROM SubscriptionInvoice i WHERE i.status = 'PENDING' AND i.dueDate <= :now")
    List<SubscriptionInvoice> findOverdueInvoices(LocalDateTime now);
    
    /**
     * Trouve les factures impayées avec un retard spécifique
     */
    @Query("SELECT i FROM SubscriptionInvoice i WHERE i.status = 'PENDING' AND i.dueDate <= :dueDate AND i.dueDate > :earlierDate")
    List<SubscriptionInvoice> findOverdueInvoicesByDueDate(LocalDateTime dueDate, LocalDateTime earlierDate);
    
    /**
     * Trouve les factures par numéro de facture
     */
    Optional<SubscriptionInvoice> findByInvoiceNumber(String invoiceNumber);
    
    /**
     * Trouve les factures d'un store par ID
     */
    @Query("SELECT i FROM SubscriptionInvoice i JOIN i.subscription s WHERE s.store.id = :storeId ORDER BY i.issuedDate DESC")
    List<SubscriptionInvoice> findByStoreId(Long storeId);
    
    /**
     * Trouve les factures par période
     */
    @Query("SELECT i FROM SubscriptionInvoice i WHERE i.issuedDate BETWEEN :startDate AND :endDate")
    List<SubscriptionInvoice> findByPeriod(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Calcule le montant total des factures payées sur une période
     */
    @Query("SELECT SUM(i.amount) FROM SubscriptionInvoice i WHERE i.status = 'PAID' AND i.paidDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalPaidAmount(LocalDateTime startDate, LocalDateTime endDate);
}
