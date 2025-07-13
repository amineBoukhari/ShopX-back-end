package com.olatech.shopxauthservice.Service.subscriptions;

import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionPlan;
import com.olatech.shopxauthservice.Repository.subscriptions.SubscriptionPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service de gestion des plans d'abonnement
 */
@Service
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;

    @Autowired
    public SubscriptionPlanServiceImpl(SubscriptionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    @Transactional
    public SubscriptionPlan createPlan(String name, String description, BigDecimal monthlyPrice, 
                                      BigDecimal yearlyPrice, int maxProducts, Integer trialPeriodDays) {
        // Vérification que le nom du plan n'existe pas déjà
        if (planRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("A plan with this name already exists");
        }
        
        // Validation des données
        if (monthlyPrice.compareTo(BigDecimal.ZERO) < 0 || yearlyPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Prices cannot be negative");
        }
        
        if (maxProducts <= 0) {
            throw new IllegalArgumentException("Maximum products must be positive");
        }
        
        // Création du plan
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(name);
        plan.setDescription(description);
        plan.setMonthlyPrice(monthlyPrice);
        plan.setYearlyPrice(yearlyPrice);
        plan.setMaxProducts(maxProducts);
        plan.setTrialPeriodDays(trialPeriodDays);
        plan.setActive(true);
        
        return planRepository.save(plan);
    }

    @Override
    @Transactional
    public SubscriptionPlan updatePlan(Long planId, String name, String description, BigDecimal monthlyPrice, 
                                      BigDecimal yearlyPrice, int maxProducts, Integer trialPeriodDays) {
        // Récupération du plan
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found with id: " + planId));
        
        // Vérification que le nouveau nom n'existe pas déjà si modifié
        if (!plan.getName().equals(name) && planRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("A plan with this name already exists");
        }
        
        // Validation des données
        if (monthlyPrice.compareTo(BigDecimal.ZERO) < 0 || yearlyPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Prices cannot be negative");
        }
        
        if (maxProducts <= 0) {
            throw new IllegalArgumentException("Maximum products must be positive");
        }
        
        // Mise à jour des informations
        plan.setName(name);
        plan.setDescription(description);
        plan.setMonthlyPrice(monthlyPrice);
        plan.setYearlyPrice(yearlyPrice);
        plan.setMaxProducts(maxProducts);
        plan.setTrialPeriodDays(trialPeriodDays);
        
        return planRepository.save(plan);
    }

    @Override
    public Optional<SubscriptionPlan> findPlanById(Long planId) {
        return planRepository.findById(planId);
    }

    @Override
    public Optional<SubscriptionPlan> findPlanByName(String name) {
        return planRepository.findByName(name);
    }

    @Override
    public List<SubscriptionPlan> findAllActivePlans() {
        return planRepository.findByIsActiveTrue();
    }

    @Override
    public List<SubscriptionPlan> findAllPlans() {
        return planRepository.findAll();
    }

    @Override
    @Transactional
    public SubscriptionPlan setActive(Long planId, boolean active) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found with id: " + planId));
        
        plan.setActive(active);
        return planRepository.save(plan);
    }

    @Override
    @Transactional
    public SubscriptionPlan addFeature(Long planId, String feature) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found with id: " + planId));
        
        plan.addFeature(feature);
        return planRepository.save(plan);
    }

    @Override
    @Transactional
    public SubscriptionPlan removeFeature(Long planId, String feature) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found with id: " + planId));
        
        plan.removeFeature(feature);
        return planRepository.save(plan);
    }
}
