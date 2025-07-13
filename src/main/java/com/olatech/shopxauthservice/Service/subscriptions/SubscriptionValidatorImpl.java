package com.olatech.shopxauthservice.Service.subscriptions;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.subscriptions.MetricType;
import com.olatech.shopxauthservice.Model.subscriptions.StoreSubscription;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionPlan;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionStatus;
import com.olatech.shopxauthservice.Repository.subscriptions.StoreSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Implémentation du service de validation des opérations liées aux abonnements
 */
@Service
public class SubscriptionValidatorImpl implements SubscriptionValidator {

    private final StoreSubscriptionRepository subscriptionRepository;
    private final UsageMetricService usageMetricService;

    @Autowired
    public SubscriptionValidatorImpl(
            StoreSubscriptionRepository subscriptionRepository,
            UsageMetricService usageMetricService) {
        this.subscriptionRepository = subscriptionRepository;
        this.usageMetricService = usageMetricService;
    }

    @Override
    public boolean canAddProduct(Store store) {
        // Vérifier que le store a un abonnement actif
        Optional<StoreSubscription> subscription = subscriptionRepository.findLatestActiveSubscriptionByStore(store);
        if (!subscription.isPresent()) {
            return false;
        }
        
        // Récupérer le nombre actuel de produits et la limite du plan
        int currentProductCount = usageMetricService.getMetricValue(store, MetricType.PRODUCT_COUNT);
        int maxProducts = subscription.get().getPlan().getMaxProducts();
        
        return currentProductCount < maxProducts;
    }

    @Override
    public boolean canAddProducts(Store store, int count) {
        // Vérifier que le store a un abonnement actif
        Optional<StoreSubscription> subscription = subscriptionRepository.findLatestActiveSubscriptionByStore(store);
        if (!subscription.isPresent()) {
            return false;
        }
        
        // Récupérer le nombre actuel de produits et la limite du plan
        int currentProductCount = usageMetricService.getMetricValue(store, MetricType.PRODUCT_COUNT);
        int maxProducts = subscription.get().getPlan().getMaxProducts();
        
        return (currentProductCount + count) <= maxProducts;
    }

    @Override
    public boolean hasFeatureAccess(Store store, String featureName) {
        // Vérifier que le store a un abonnement actif
        Optional<StoreSubscription> subscription = subscriptionRepository.findLatestActiveSubscriptionByStore(store);
        if (!subscription.isPresent()) {
            return false;
        }
        
        // Vérifier si le plan inclut la fonctionnalité
        return subscription.get().getPlan().getFeatures().contains(featureName);
    }

    @Override
    public boolean isSubscriptionActive(StoreSubscription subscription) {
        if (subscription == null) {
            return false;
        }
        
        // Un abonnement est actif s'il a le statut ACTIVE ou TRIAL et qu'il n'est pas expiré
        boolean isActiveStatus = subscription.getStatus() == SubscriptionStatus.ACTIVE 
                             || subscription.getStatus() == SubscriptionStatus.TRIAL;
        
        boolean isNotExpired = !isSubscriptionExpired(subscription);
        
        return isActiveStatus && isNotExpired;
    }

    @Override
    public boolean isInTrialPeriod(StoreSubscription subscription) {
        if (subscription == null) {
            return false;
        }
        
        return subscription.getStatus() == SubscriptionStatus.TRIAL 
            && subscription.getTrialEndDate() != null 
            && subscription.getTrialEndDate().isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isSubscriptionExpired(StoreSubscription subscription) {
        if (subscription == null) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Un abonnement est expiré si sa date de fin est passée
        boolean isEndDatePassed = subscription.getEndDate().isBefore(now);
        
        // Pour les abonnements en période d'essai, vérifier aussi la date de fin d'essai
        if (subscription.getStatus() == SubscriptionStatus.TRIAL) {
            boolean isTrialEndDatePassed = subscription.getTrialEndDate() != null 
                                       && subscription.getTrialEndDate().isBefore(now);
            
            return isEndDatePassed || isTrialEndDatePassed;
        }
        
        return isEndDatePassed;
    }

    @Override
    public boolean isPlanChangeValid(SubscriptionPlan currentPlan, SubscriptionPlan newPlan) {
        if (currentPlan == null || newPlan == null) {
            return false;
        }
        
        // Vérifier que le nouveau plan est actif
        if (!newPlan.isActive()) {
            return false;
        }
        
        // Les règles métier spécifiques peuvent être ajoutées ici
        // Par exemple, interdire le passage d'un plan supérieur à un plan inférieur
        // Ou autoriser uniquement les mises à niveau vers des plans avec plus de fonctionnalités
        
        return true;
    }

    @Override
    public boolean isOverLimit(Store store) {
        // Vérifier que le store a un abonnement actif
        Optional<StoreSubscription> subscription = subscriptionRepository.findLatestActiveSubscriptionByStore(store);
        if (!subscription.isPresent()) {
            return true; // Considérer qu'un store sans abonnement est en dépassement
        }
        
        // Vérifier pour chaque type de métrique si la limite est dépassée
        int productCount = usageMetricService.getMetricValue(store, MetricType.PRODUCT_COUNT);
        int maxProducts = subscription.get().getPlan().getMaxProducts();
        
        if (productCount > maxProducts) {
            return true;
        }
        
        // Ajouter d'autres vérifications selon les besoins
        
        return false;
    }

    @Override
    public int daysUntilExpiration(StoreSubscription subscription) {
        if (subscription == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration;
        
        // Utiliser la date de fin d'essai si en période d'essai, sinon la date de fin régulière
        if (subscription.getStatus() == SubscriptionStatus.TRIAL && subscription.getTrialEndDate() != null) {
            expiration = subscription.getTrialEndDate().isBefore(subscription.getEndDate()) 
                       ? subscription.getTrialEndDate() 
                       : subscription.getEndDate();
        } else {
            expiration = subscription.getEndDate();
        }
        
        // Si déjà expiré, retourner 0
        if (expiration.isBefore(now)) {
            return 0;
        }
        
        // Calculer le nombre de jours entre maintenant et la date d'expiration
        return (int) ChronoUnit.DAYS.between(now, expiration);
    }

    @Override
    public boolean isEligibleForAutoRenewal(StoreSubscription subscription) {
        if (subscription == null) {
            return false;
        }
        
        // Un abonnement est éligible au renouvellement automatique s'il est actif,
        // que l'option de renouvellement automatique est activée et qu'il n'est pas déjà expiré
        return subscription.getStatus() == SubscriptionStatus.ACTIVE 
            && subscription.isAutoRenew() 
            && !isSubscriptionExpired(subscription);
    }
}
