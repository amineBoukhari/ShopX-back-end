package com.olatech.shopxauthservice.Service.subscriptions;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.subscriptions.*;
import com.olatech.shopxauthservice.Repository.StoreRepository;
import com.olatech.shopxauthservice.Repository.subscriptions.StoreSubscriptionRepository;
import com.olatech.shopxauthservice.Repository.subscriptions.UsageMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service de gestion des abonnements
 */
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final StoreSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanService planService;
    private final SubscriptionInvoiceService invoiceService;
    private final UsageMetricService usageMetricService;
    private final UsageMetricRepository usageMetricRepository;
    private final SubscriptionValidator validator;
    private final StoreRepository storeRepository;

    @Autowired
    public SubscriptionServiceImpl(
            StoreSubscriptionRepository subscriptionRepository,
            SubscriptionPlanService planService,
            SubscriptionInvoiceService invoiceService,
            UsageMetricService usageMetricService,
            UsageMetricRepository usageMetricRepository,
            SubscriptionValidator validator,
            StoreRepository storeRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planService = planService;
        this.invoiceService = invoiceService;
        this.usageMetricService = usageMetricService;
        this.usageMetricRepository = usageMetricRepository;
        this.validator = validator;
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional
    public StoreSubscription createTrialSubscription(Store store, SubscriptionPlan plan, BillingCycle billingCycle) {
        // Vérifier si le store a déjà un abonnement actif
        Optional<StoreSubscription> existingSubscription = findActiveSubscription(store);
        if (existingSubscription.isPresent()) {
            throw new IllegalStateException("Store already has an active subscription");
        }

        // Créer le nouvel abonnement
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate;
        
        if (billingCycle == BillingCycle.MONTHLY) {
            endDate = now.plusMonths(1);
        } else {
            endDate = now.plusYears(1);
        }
        
        StoreSubscription subscription = new StoreSubscription();
        subscription.setStore(store);
        subscription.setPlan(plan);
        subscription.setStartDate(now);
        subscription.setEndDate(endDate);
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setBillingCycle(billingCycle);
        subscription.setAutoRenew(true);
        
        // Définir la fin de la période d'essai
        if (plan.getTrialPeriodDays() != null && plan.getTrialPeriodDays() > 0) {
            subscription.setTrialEndDate(now.plusDays(plan.getTrialPeriodDays()));
        } else {
            // Période d'essai par défaut de 14 jours si non spécifiée
            subscription.setTrialEndDate(now.plusDays(14));
        }
        
        subscription.setNextBillingDate(subscription.getTrialEndDate());
        
        // Sauvegarder l'abonnement
        subscription = subscriptionRepository.save(subscription);
        
        // Initialiser les métriques d'utilisation
        initializeUsageMetrics(store);
        
        return subscription;
    }

    @Override
    @Transactional
    public StoreSubscription createTrialSubscriptionByStoreId(Long storeId, SubscriptionPlan plan, BillingCycle billingCycle) {
        // Récupérer le store par son ID
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + storeId));
        
        // Déléguer à la méthode existante
        return createTrialSubscription(store, plan, billingCycle);
    }

    @Override
    @Transactional
    public StoreSubscription createSubscription(Store store, SubscriptionPlan plan, BillingCycle billingCycle) {
        // Vérifier si le store a déjà un abonnement actif
        Optional<StoreSubscription> existingSubscription = findActiveSubscription(store);
        if (existingSubscription.isPresent()) {
            throw new IllegalStateException("Store already has an active subscription");
        }

        // Créer le nouvel abonnement
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate;
        
        if (billingCycle == BillingCycle.MONTHLY) {
            endDate = now.plusMonths(1);
        } else {
            endDate = now.plusYears(1);
        }
        
        StoreSubscription subscription = new StoreSubscription();
        subscription.setStore(store);
        subscription.setPlan(plan);
        subscription.setStartDate(now);
        subscription.setEndDate(endDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setBillingCycle(billingCycle);
        subscription.setAutoRenew(true);
        subscription.setNextBillingDate(endDate);
        
        // Sauvegarder l'abonnement
        subscription = subscriptionRepository.save(subscription);
        
        // Générer une facture
        invoiceService.generateInvoice(
            subscription, 
            billingCycle == BillingCycle.MONTHLY ? plan.getMonthlyPrice() : plan.getYearlyPrice(),
            now.plusDays(7) // Échéance à 7 jours
        );
        
        // Initialiser les métriques d'utilisation
        initializeUsageMetrics(store);
        
        return subscription;
    }

    @Override
    public Optional<StoreSubscription> findActiveSubscription(Store store) {
        return subscriptionRepository.findLatestActiveSubscriptionByStore(store);
    }

    @Override
    public Optional<StoreSubscription> findActiveSubscriptionByStoreId(Long storeId) {
        return subscriptionRepository.findLatestActiveSubscriptionByStoreId(storeId);
    }

    @Override
    public List<StoreSubscription> findAllSubscriptions(Store store) {
        return subscriptionRepository.findByStoreOrderByStartDateDesc(store);
    }

    @Override
    public List<StoreSubscription> findAllSubscriptionsByStoreId(Long storeId) {
        // Récupérer le store par son ID
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + storeId));
        
        // Déléguer à la méthode existante
        return findAllSubscriptions(store);
    }

    @Override
    @Transactional
    public StoreSubscription updateSubscription(StoreSubscription subscription) {
        // Sauvegarde simple de l'abonnement mis à jour
        return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public StoreSubscription changePlan(StoreSubscription subscription, SubscriptionPlan newPlan, BillingCycle billingCycle) {
        // Vérifier si le changement est valide
        if (!validator.isPlanChangeValid(subscription.getPlan(), newPlan)) {
            throw new IllegalStateException("Invalid plan change");
        }

        // Calcul de la nouvelle date de fin
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate;
        
        if (billingCycle == BillingCycle.MONTHLY) {
            endDate = now.plusMonths(1);
        } else {
            endDate = now.plusYears(1);
        }
        
        // Mettre à jour l'abonnement
        subscription.setPlan(newPlan);
        subscription.setBillingCycle(billingCycle);
        subscription.setStartDate(now);
        subscription.setEndDate(endDate);
        subscription.setNextBillingDate(endDate);
        
        // Si en période d'essai, conserver ce statut
        if (subscription.getStatus() != SubscriptionStatus.TRIAL) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }
        
        // Sauvegarder l'abonnement
        subscription = subscriptionRepository.save(subscription);
        
        // Générer une facture si non en période d'essai
        if (subscription.getStatus() != SubscriptionStatus.TRIAL) {
            invoiceService.generateInvoice(
                subscription,
                billingCycle == BillingCycle.MONTHLY ? newPlan.getMonthlyPrice() : newPlan.getYearlyPrice(),
                now.plusDays(7) // Échéance à 7 jours
            );
        }
        
        return subscription;
    }

    @Override
    @Transactional
    public StoreSubscription renewSubscription(StoreSubscription subscription) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate;
        
        // Calculer la nouvelle date de fin
        if (subscription.getBillingCycle() == BillingCycle.MONTHLY) {
            endDate = now.plusMonths(1);
        } else {
            endDate = now.plusYears(1);
        }
        
        // Mettre à jour l'abonnement
        subscription.setStartDate(now);
        subscription.setEndDate(endDate);
        subscription.setNextBillingDate(endDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        
        // Sauvegarder l'abonnement
        subscription = subscriptionRepository.save(subscription);
        
        // Générer une facture de renouvellement
        invoiceService.generateRenewalInvoice(subscription);
        
        return subscription;
    }

    @Override
    @Transactional
    public StoreSubscription cancelSubscription(StoreSubscription subscription, LocalDateTime effectiveDate) {
        // Vérifier la date effective d'annulation
        if (effectiveDate == null) {
            // Annulation immédiate
            subscription.setStatus(SubscriptionStatus.CANCELED);
        } else if (effectiveDate.isAfter(LocalDateTime.now())) {
            // Programmation de l'annulation
            subscription.setEndDate(effectiveDate);
            // Le statut sera mis à jour par le scheduler à la date effective
        } else {
            throw new IllegalArgumentException("Effective date must be in the future");
        }
        
        // Désactiver le renouvellement automatique
        subscription.setAutoRenew(false);
        
        // Sauvegarder l'abonnement
        return subscriptionRepository.save(subscription);
    }

    @Override
    public boolean canAddProduct(Store store) {
        Optional<StoreSubscription> subscription = findActiveSubscription(store);
        if (!subscription.isPresent()) {
            return false;
        }
        
        int currentProductCount = usageMetricService.getMetricValue(store, MetricType.PRODUCT_COUNT);
        int maxProducts = subscription.get().getPlan().getMaxProducts();
        
        return currentProductCount < maxProducts;
    }

    @Override
    public boolean canAddProducts(Store store, int count) {
        Optional<StoreSubscription> subscription = findActiveSubscription(store);
        if (!subscription.isPresent()) {
            return false;
        }
        
        int currentProductCount = usageMetricService.getMetricValue(store, MetricType.PRODUCT_COUNT);
        int maxProducts = subscription.get().getPlan().getMaxProducts();
        
        return (currentProductCount + count) <= maxProducts;
    }

    @Override
    @Transactional
    public void updateProductCount(Store store, int count) {
        usageMetricService.updateMetric(store, MetricType.PRODUCT_COUNT, count);
    }

    @Override
    public List<StoreSubscription> findExpiringSubscriptions(int daysThreshold) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thresholdDate = now.plusDays(daysThreshold);
        
        return subscriptionRepository.findExpiringSubscriptions(now, thresholdDate);
    }

    @Override
    @Transactional
    public int processAutoRenewals() {
        LocalDateTime now = LocalDateTime.now();
        // Trouver les abonnements qui doivent être renouvelés aujourd'hui
        List<StoreSubscription> renewalSubscriptions = subscriptionRepository.findSubscriptionsForAutoRenewal(
            now.minusHours(1), // Petite marge pour éviter de manquer des abonnements
            now.plusHours(1)
        );
        
        int count = 0;
        for (StoreSubscription subscription : renewalSubscriptions) {
            if (subscription.isAutoRenew()) {
                renewSubscription(subscription);
                count++;
            }
        }
        
        return count;
    }

    @Override
    @Transactional
    public SubscriptionInvoice generateInvoice(StoreSubscription subscription) {
        // Déterminer le montant de la facture en fonction du cycle de facturation
        java.math.BigDecimal amount;
        if (subscription.getBillingCycle() == BillingCycle.MONTHLY) {
            amount = subscription.getPlan().getMonthlyPrice();
        } else {
            amount = subscription.getPlan().getYearlyPrice();
        }
        
        // Générer la facture avec échéance à 7 jours
        return invoiceService.generateInvoice(subscription, amount, LocalDateTime.now().plusDays(7));
    }
    
    @Override
    public Optional<Store> getStoreById(Long storeId) {
        return storeRepository.findById(storeId);
    }
    
    /**
     * Initialise les métriques d'utilisation pour un nouveau store
     */
    private void initializeUsageMetrics(Store store) {
        usageMetricService.updateMetric(store, MetricType.PRODUCT_COUNT, 0);
        usageMetricService.updateMetric(store, MetricType.API_CALLS, 0);
        usageMetricService.updateMetric(store, MetricType.BANDWIDTH_USAGE, 0);
        usageMetricService.updateMetric(store, MetricType.STORAGE_USAGE, 0);
    }
}
