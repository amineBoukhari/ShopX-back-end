package com.olatech.shopxauthservice.Controller.subscriptions;

import com.olatech.shopxauthservice.Model.subscriptions.*;
import com.olatech.shopxauthservice.Service.StoreService;
import com.olatech.shopxauthservice.Service.subscriptions.SubscriptionInvoiceService;
import com.olatech.shopxauthservice.Service.subscriptions.SubscriptionPlanService;
import com.olatech.shopxauthservice.Service.subscriptions.SubscriptionService;
import com.olatech.shopxauthservice.Service.subscriptions.UsageMetricService;
import com.olatech.shopxauthservice.DTO.subscriptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionPlanService planService;
    private final SubscriptionInvoiceService invoiceService;
    private final UsageMetricService usageMetricService;
    private final StoreService storeService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService,
                                 SubscriptionPlanService planService,
                                    StoreService storeService,
                                 SubscriptionInvoiceService invoiceService,
                                 UsageMetricService usageMetricService) {
        this.subscriptionService = subscriptionService;
        this.planService = planService;
        this.invoiceService = invoiceService;
        this.usageMetricService = usageMetricService;
        this.storeService = storeService;
    }

    // ==========================
    // PLANS D'ABONNEMENT
    // ==========================

    /**
     * Liste tous les plans d'abonnement actifs
     */
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlan>> getActivePlans() {
        List<SubscriptionPlan> plans = planService.findAllActivePlans();
        return ResponseEntity.ok(plans);
    }

    /**
     * Récupère les détails d'un plan spécifique
     */
    @GetMapping("/plans/{planId}")
    public ResponseEntity<SubscriptionPlan> getPlanDetails(@PathVariable Long planId) {
        return planService.findPlanById(planId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================
    // ABONNEMENTS DE STORE
    // ==========================

    /**
     * Récupère l'abonnement actif d'un store
     */
    @GetMapping("/stores/{storeId}/active")
    public ResponseEntity<StoreSubscription> getActiveSubscription(@PathVariable Long storeId) {
        return subscriptionService.findActiveSubscriptionByStoreId(storeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupère tous les abonnements d'un store (historique)
     */
    @GetMapping("/stores/{storeId}/history")
    public ResponseEntity<List<StoreSubscription>> getSubscriptionHistory(@PathVariable Long storeId) {
        // Nous n'avons pas cette méthode exacte, mais on peut l'implémenter
        // Pour l'exemple, on va supposer qu'elle existe
        List<StoreSubscription> subscriptions = subscriptionService.findAllSubscriptionsByStoreId(storeId);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Crée un nouvel abonnement avec période d'essai
     */
    @PostMapping("/stores/{storeId}/subscribe")
    public ResponseEntity<StoreSubscription> subscribeToTrial(
            @PathVariable Long storeId,
            @RequestBody SubscriptionRequest request) {
        
        // Vérifier que le plan existe
        return planService.findPlanById(request.getPlanId())
                .map(plan -> {
                    // Créer un abonnement avec période d'essai
                    StoreSubscription subscription = subscriptionService.createTrialSubscriptionByStoreId(
                            storeId, plan, request.getBillingCycle());
                    
                    return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * Change le plan d'abonnement d'un store
     */
    @PutMapping("/stores/{storeId}/change-plan")
    public ResponseEntity<StoreSubscription> changePlan(
            @PathVariable Long storeId,
            @RequestBody ChangePlanRequest request) {
        
        // Vérifier que le store a un abonnement actif
        return subscriptionService.findActiveSubscriptionByStoreId(storeId)
                .map(subscription -> {
                    // Vérifier que le nouveau plan existe
                    return planService.findPlanById(request.getNewPlanId())
                            .map(newPlan -> {
                                // Changer le plan
                                StoreSubscription updatedSubscription = 
                                    subscriptionService.changePlan(subscription, newPlan, request.getBillingCycle());
                                
                                return ResponseEntity.ok(updatedSubscription);
                            })
                            .orElse(ResponseEntity.badRequest().build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Annule un abonnement (immédiatement ou à la fin de la période)
     */
    @PostMapping("/stores/{storeId}/cancel")
    public ResponseEntity<StoreSubscription> cancelSubscription(
            @PathVariable Long storeId,
            @RequestBody CancelSubscriptionRequest request) {
        
        // Vérifier que le store a un abonnement actif
        return subscriptionService.findActiveSubscriptionByStoreId(storeId)
                .map(subscription -> {
                    // Déterminer la date effective d'annulation
                    LocalDateTime effectiveDate = request.isCancelImmediately() 
                            ? LocalDateTime.now() 
                            : subscription.getEndDate();
                    
                    // Annuler l'abonnement
                    StoreSubscription canceledSubscription = 
                        subscriptionService.cancelSubscription(subscription, effectiveDate);
                    
                    return ResponseEntity.ok(canceledSubscription);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Active/Désactive le renouvellement automatique
     */
    @PutMapping("/stores/{storeId}/auto-renew")
    public ResponseEntity<StoreSubscription> setAutoRenew(
            @PathVariable Long storeId,
            @RequestBody AutoRenewRequest request) {
        
        // Vérifier que le store a un abonnement actif
        return subscriptionService.findActiveSubscriptionByStoreId(storeId)
                .map(subscription -> {
                    // Mettre à jour le renouvellement automatique
                    subscription.setAutoRenew(request.isAutoRenew());
                    StoreSubscription updatedSubscription = subscriptionService.updateSubscription(subscription);
                    
                    return ResponseEntity.ok(updatedSubscription);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================
    // FACTURES
    // ==========================

    /**
     * Liste toutes les factures d'un store
     */
    @GetMapping("/stores/{storeId}/invoices")
    public ResponseEntity<List<SubscriptionInvoice>> getInvoices(@PathVariable Long storeId) {
        List<SubscriptionInvoice> invoices = invoiceService.findInvoicesByStoreId(storeId);
        return ResponseEntity.ok(invoices);
    }

    /**
     * Récupère les détails d'une facture spécifique
     */
    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<SubscriptionInvoice> getInvoiceDetails(@PathVariable Long invoiceId) {
        return invoiceService.findInvoiceById(invoiceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Marque une facture comme payée (simulation de paiement)
     */
    @PostMapping("/invoices/{invoiceId}/pay")
    public ResponseEntity<SubscriptionInvoice> payInvoice(
            @PathVariable Long invoiceId,
            @RequestBody PaymentRequest request) {
        
        return invoiceService.findInvoiceById(invoiceId)
                .map(invoice -> {
                    SubscriptionInvoice paidInvoice = invoiceService.markAsPaid(
                            invoiceId, request.getPaymentMethod(), LocalDateTime.now());
                    
                    return ResponseEntity.ok(paidInvoice);
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
