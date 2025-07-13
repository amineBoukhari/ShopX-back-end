package com.olatech.shopxauthservice.Service.subscriptions;

import com.olatech.shopxauthservice.Model.subscriptions.BillingCycle;
import com.olatech.shopxauthservice.Model.subscriptions.InvoiceStatus;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionInvoice;
import com.olatech.shopxauthservice.Model.subscriptions.StoreSubscription;
import com.olatech.shopxauthservice.Repository.subscriptions.SubscriptionInvoiceRepository;
import com.olatech.shopxauthservice.Repository.subscriptions.StoreSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implémentation du service de gestion des factures d'abonnement
 */
@Service
public class SubscriptionInvoiceServiceImpl implements SubscriptionInvoiceService {

    private final SubscriptionInvoiceRepository invoiceRepository;
    private final StoreSubscriptionRepository subscriptionRepository;

    @Autowired
    public SubscriptionInvoiceServiceImpl(
            SubscriptionInvoiceRepository invoiceRepository,
            StoreSubscriptionRepository subscriptionRepository) {
        this.invoiceRepository = invoiceRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional
    public SubscriptionInvoice generateInvoice(StoreSubscription subscription, BigDecimal amount, LocalDateTime dueDate) {
        // Vérification des paramètres
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        
        if (dueDate == null || dueDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Due date must be in the future");
        }
        
        // Création de la facture
        SubscriptionInvoice invoice = new SubscriptionInvoice();
        invoice.setSubscription(subscription);
        invoice.setAmount(amount);
        invoice.setIssuedDate(LocalDateTime.now());
        invoice.setDueDate(dueDate);
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        
        return invoiceRepository.save(invoice);
    }

    @Override
    public Optional<SubscriptionInvoice> findInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId);
    }

    @Override
    public List<SubscriptionInvoice> findInvoicesBySubscriptionId(Long subscriptionId) {
        return invoiceRepository.findBySubscriptionIdOrderByIssuedDateDesc(subscriptionId);
    }

    @Override
    public List<SubscriptionInvoice> findInvoicesByStoreId(Long storeId) {
        return invoiceRepository.findByStoreId(storeId);
    }

    @Override
    @Transactional
    public SubscriptionInvoice markAsPaid(Long invoiceId, String paymentMethod, LocalDateTime paidDate) {
        SubscriptionInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with id: " + invoiceId));
        
        // Vérifie que la facture n'est pas déjà payée ou annulée
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice is already paid");
        }
        
        if (invoice.getStatus() == InvoiceStatus.CANCELED) {
            throw new IllegalStateException("Cannot pay a canceled invoice");
        }
        
        // Mise à jour des informations de paiement
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setPaidDate(paidDate != null ? paidDate : LocalDateTime.now());
        
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public SubscriptionInvoice updateStatus(Long invoiceId, InvoiceStatus status) {
        SubscriptionInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with id: " + invoiceId));
        
        invoice.setStatus(status);
        
        // Si la facture est marquée comme payée, mettre à jour la date de paiement
        if (status == InvoiceStatus.PAID && invoice.getPaidDate() == null) {
            invoice.setPaidDate(LocalDateTime.now());
        }
        
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public SubscriptionInvoice cancelInvoice(Long invoiceId, String reason) {
        SubscriptionInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with id: " + invoiceId));
        
        // Vérifie que la facture n'est pas déjà payée
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Cannot cancel a paid invoice");
        }
        
        // Met à jour le statut et ajoute le motif d'annulation
        invoice.setStatus(InvoiceStatus.CANCELED);
        invoice.setNotes(reason);
        
        return invoiceRepository.save(invoice);
    }

    @Override
    public List<SubscriptionInvoice> findOverdueInvoices(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.minusDays(days);
        
        return invoiceRepository.findOverdueInvoicesByDueDate(now, dueDate);
    }

    @Override
    @Transactional
    public SubscriptionInvoice generateRenewalInvoice(StoreSubscription subscription) {
        // Vérification que l'abonnement est valide
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }
        
        // Calcul du montant de la facture en fonction du cycle de facturation
        BigDecimal amount;
        if (subscription.getBillingCycle() == BillingCycle.MONTHLY) {
            amount = subscription.getPlan().getMonthlyPrice();
        } else {
            amount = subscription.getPlan().getYearlyPrice();
        }
        
        // Création de la facture avec échéance à 7 jours
        SubscriptionInvoice invoice = new SubscriptionInvoice();
        invoice.setSubscription(subscription);
        invoice.setAmount(amount);
        invoice.setIssuedDate(LocalDateTime.now());
        invoice.setDueDate(LocalDateTime.now().plusDays(7));
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setNotes("Renewal invoice for subscription #" + subscription.getId());
        
        return invoiceRepository.save(invoice);
    }

    @Override
    public String generateInvoiceNumber() {
        // Format: INV-YYYYMMDD-XXXX où XXXX est un nombre aléatoire
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
        
        return "INV-" + datePart + "-" + randomNum;
    }

    @Override
    public BigDecimal calculateTotalInvoiceAmount(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceRepository.getTotalPaidAmount(startDate, endDate);
    }
}
