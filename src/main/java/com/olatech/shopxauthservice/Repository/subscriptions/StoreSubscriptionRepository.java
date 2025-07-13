package com.olatech.shopxauthservice.Repository.subscriptions;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.subscriptions.BillingCycle;
import com.olatech.shopxauthservice.Model.subscriptions.StoreSubscription;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionPlan;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreSubscriptionRepository extends JpaRepository<StoreSubscription, Long> {
    
    /**
     * Trouve les abonnements actifs d'un store
     */
    @Query("SELECT s FROM StoreSubscription s WHERE s.store = :store AND (s.status = 'ACTIVE' OR s.status = 'TRIAL')")
    List<StoreSubscription> findActiveSubscriptionsByStore(Store store);
    
    /**
     * Trouve l'abonnement actif d'un store (devrait être au plus un)
     */
    @Query("SELECT s FROM StoreSubscription s WHERE s.store = :store AND (s.status = 'ACTIVE' OR s.status = 'TRIAL') ORDER BY s.endDate DESC")
    Optional<StoreSubscription> findLatestActiveSubscriptionByStore(Store store);
    
    /**
     * Trouve l'abonnement actif d'un store par son ID
     */
    @Query("SELECT s FROM StoreSubscription s WHERE s.store.id = :storeId AND (s.status = 'ACTIVE' OR s.status = 'TRIAL') ORDER BY s.endDate DESC")
    Optional<StoreSubscription> findLatestActiveSubscriptionByStoreId(Long storeId);
    
    /**
     * Trouve tous les abonnements d'un store
     */
    List<StoreSubscription> findByStoreOrderByStartDateDesc(Store store);
    
    /**
     * Trouve les abonnements par plan
     */
    List<StoreSubscription> findByPlan(SubscriptionPlan plan);
    
    /**
     * Trouve les abonnements par statut
     */
    List<StoreSubscription> findByStatus(SubscriptionStatus status);
    
    /**
     * Trouve les abonnements par cycle de facturation
     */
    List<StoreSubscription> findByBillingCycle(BillingCycle billingCycle);
    
    /**
     * Trouve les abonnements qui expirent bientôt
     */
    @Query("SELECT s FROM StoreSubscription s WHERE s.status = 'ACTIVE' AND s.endDate BETWEEN :now AND :expirationDate")
    List<StoreSubscription> findExpiringSubscriptions(LocalDateTime now, LocalDateTime expirationDate);
    
    /**
     * Trouve les abonnements en période d'essai qui se terminent bientôt
     */
    @Query("SELECT s FROM StoreSubscription s WHERE s.status = 'TRIAL' AND s.trialEndDate BETWEEN :now AND :endDate")
    List<StoreSubscription> findEndingTrialSubscriptions(LocalDateTime now, LocalDateTime endDate);
    
    /**
     * Trouve les abonnements à renouveler automatiquement
     */
    @Query("SELECT s FROM StoreSubscription s WHERE s.status = 'ACTIVE' AND s.autoRenew = true AND s.endDate BETWEEN :now AND :renewalDate")
    List<StoreSubscription> findSubscriptionsForAutoRenewal(LocalDateTime now, LocalDateTime renewalDate);
}
