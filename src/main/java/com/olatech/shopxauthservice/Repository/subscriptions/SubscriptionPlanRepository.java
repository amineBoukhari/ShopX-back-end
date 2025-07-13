package com.olatech.shopxauthservice.Repository.subscriptions;

import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    
    /**
     * Trouve un plan par son nom
     */
    Optional<SubscriptionPlan> findByName(String name);
    
    /**
     * Trouve tous les plans actifs
     */
    List<SubscriptionPlan> findByIsActiveTrue();
    
    /**
     * Trouve les plans par prix mensuel inférieur à une valeur
     */
    @Query("SELECT p FROM SubscriptionPlan p WHERE p.monthlyPrice <= :price AND p.isActive = true")
    List<SubscriptionPlan> findActivePlansByMonthlyPriceLessThanEqual(java.math.BigDecimal price);
    
    /**
     * Trouve les plans par nombre maximum de produits supérieur à une valeur
     */
    List<SubscriptionPlan> findByMaxProductsGreaterThanEqualAndIsActiveTrue(int maxProducts);
    
    /**
     * Trouve les plans qui ont une période d'essai
     */
    @Query("SELECT p FROM SubscriptionPlan p WHERE p.trialPeriodDays > 0 AND p.isActive = true")
    List<SubscriptionPlan> findActiveTrialPlans();
}
