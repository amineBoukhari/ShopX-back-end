package com.olatech.shopxauthservice.aspect;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.subscriptions.MetricType;
import com.olatech.shopxauthservice.Model.subscriptions.StoreSubscription;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionPlan;
import com.olatech.shopxauthservice.Service.subscriptions.SubscriptionScheduler;
import com.olatech.shopxauthservice.Service.subscriptions.SubscriptionService;
import com.olatech.shopxauthservice.Service.subscriptions.SubscriptionValidator;
import com.olatech.shopxauthservice.Service.subscriptions.UsageMetricService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Aspect pour vérifier les limites des plans d'abonnement
 * avant l'exécution des opérations sensibles.
 */
@Aspect
@Component
public class SubscriptionLimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionLimitAspect.class);
    private final ExpressionParser parser = new SpelExpressionParser();

    private final SubscriptionService subscriptionService;
    private final SubscriptionValidator subscriptionValidator;
    private final UsageMetricService usageMetricService;

    @Autowired
    public SubscriptionLimitAspect(SubscriptionService subscriptionService, SubscriptionValidator subscriptionValidator,
                                   UsageMetricService usageMetricService) {
        this.subscriptionService = subscriptionService;
        this.subscriptionValidator = subscriptionValidator;
        this.usageMetricService = usageMetricService;
    }

    /**
     * Point de coupe pour intercepter les méthodes annotées avec @CheckSubscriptionLimit
     */
    @Before("@annotation(checkLimit)")
    public void checkSubscriptionLimit(JoinPoint joinPoint, CheckSubscriptionLimit checkLimit) {
        // Extraire les arguments de la méthode
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = signature.getParameterNames();
        
        // Récupérer le Store ou l'ID du store
        Store store = null;
        Long storeId = null;
        
        // Afficher les paramètres pour debugging
        logger.debug("Method parameters: ");
        for (int i = 0; i < paramNames.length; i++) {
            logger.debug("  {} ({}): {}", paramNames[i], args[i] != null ? args[i].getClass().getSimpleName() : "null", args[i]);
        }
        
        // Si un paramètre spécifique est défini dans l'annotation
        if (!checkLimit.storeIdParam().isEmpty()) {
            String storeIdParam = checkLimit.storeIdParam();
            
            // Utiliser Spring Expression Language pour extraire la valeur
            StandardEvaluationContext context = new StandardEvaluationContext();
            
            // Ajouter les arguments de la méthode au contexte
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            
            // Évaluer l'expression
            Expression exp = parser.parseExpression("#" + storeIdParam);
            Object value = exp.getValue(context);
            
            if (value instanceof Store) {
                store = (Store) value;
            } else if (value instanceof Long || value instanceof Integer) {
                storeId = ((Number) value).longValue();
            } else {
                throw new IllegalArgumentException("Cannot extract Store or storeId from method parameters");
            }
        } else {
            // Sinon, chercher un paramètre de type Store ou un paramètre nommé storeId
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Store) {
                    store = (Store) args[i];
                    break;
                }
                
                if ("storeId".equals(paramNames[i]) && args[i] != null) {
                    if (args[i] instanceof Long) {
                        storeId = (Long) args[i];
                    } else if (args[i] instanceof Integer) {
                        storeId = ((Integer) args[i]).longValue();
                    } else if (args[i] instanceof String) {
                        // Ajout de la gestion des String pour storeId
                        try {
                            storeId = Long.parseLong((String) args[i]);
                        } catch (NumberFormatException e) {
                            logger.error("Failed to parse storeId from String: {}", args[i]);
                            throw new IllegalArgumentException("storeId must be a valid number");
                        }
                    }
                    break;
                }
            }
        }
        
        // Si on n'a pas trouvé de store ou de storeId, lever une exception
        if (store == null && storeId == null) {
            throw new IllegalArgumentException("Cannot extract Store or storeId from method parameters");
        }
        
        // Récupérer l'abonnement actif
        Optional<StoreSubscription> optSubscription;
        if (store != null) {
            optSubscription = subscriptionService.findActiveSubscription(store);
        } else {
            optSubscription = subscriptionService.findActiveSubscriptionByStoreId(storeId);
            if (optSubscription.isPresent() && store == null) {
                store = optSubscription.get().getStore();
            }
        }
        
        // Vérifier que l'abonnement existe
        if (!optSubscription.isPresent()) {
            throw new SubscriptionLimitExceededException(
                "No active subscription found for this operation", checkLimit.type());
        }
        
        StoreSubscription subscription = optSubscription.get();
        SubscriptionPlan plan = subscription.getPlan();
        
        // Vérifier la limite selon le type
        switch (checkLimit.type()) {
            case PRODUCT:
                checkProductLimit(store, plan, checkLimit.amount());
                break;
            case API_CALL:
                checkApiCallLimit(store, plan, checkLimit.amount());
                break;
            case STORAGE:
                checkStorageLimit(store, plan, checkLimit.amount());
                break;
            case BANDWIDTH:
                checkBandwidthLimit(store, plan, checkLimit.amount());
                break;
            case FEATURE:
                // Pour les fonctionnalités, on extrait le nom de la fonctionnalité à partir du nom de la méthode
                String featureName = method.getName().toLowerCase();
                checkFeatureAccess(plan, featureName);
                break;
            default:
                logger.warn("Unknown limit type: {}", checkLimit.type());
        }
        
        // Si l'incrémentation est activée, mettre à jour le compteur
        if (checkLimit.increment() && store != null) {
            updateMetric(store, checkLimit.type(), checkLimit.amount());
        }
    }
    
    /**
     * Vérifie si le store peut ajouter des produits selon son plan
     */
    private void checkProductLimit(Store store, SubscriptionPlan plan, int amount) {

        boolean canAddMore = subscriptionValidator.canAddProducts(store, amount);
        int maxAllowed = plan.getMaxProducts();
        System.out.println("Max allowed products: " + maxAllowed);
        System.out.println("Amount to add: " + amount);
        System.out.println("Can add more: " + canAddMore);
        if (!canAddMore) {
            throw new SubscriptionLimitExceededException(
                "Product limit exceeded. Cannot add more products with current subscription plan.",
                LimitType.PRODUCT, amount, maxAllowed);
        }
    }
    
    /**
     * Vérifie si le store peut effectuer des appels API selon son plan
     */
    private void checkApiCallLimit(Store store, SubscriptionPlan plan, int amount) {
        int currentCount = usageMetricService.getMetricValue(store, MetricType.API_CALLS);
        
        // La limite d'appels API pourrait être stockée dans les fonctionnalités du plan
        // Ici, on utilise une valeur par défaut
        int maxAllowed = 10000; // Valeur fictive
        
        if (currentCount + amount > maxAllowed) {
            throw new SubscriptionLimitExceededException(
                "API call limit exceeded. Please upgrade your subscription plan.",
                LimitType.API_CALL, currentCount, maxAllowed);
        }
    }
    
    /**
     * Vérifie si le store peut utiliser plus de stockage selon son plan
     */
    private void checkStorageLimit(Store store, SubscriptionPlan plan, int amountInMB) {
        int currentUsage = usageMetricService.getMetricValue(store, MetricType.STORAGE_USAGE);
        
        // La limite de stockage pourrait être stockée dans les fonctionnalités du plan
        // Ici, on utilise une valeur par défaut
        int maxAllowed = 1000; // Valeur fictive en MB
        
        if (currentUsage + amountInMB > maxAllowed) {
            throw new SubscriptionLimitExceededException(
                "Storage limit exceeded. Please upgrade your subscription plan.",
                LimitType.STORAGE, currentUsage, maxAllowed);
        }
    }
    
    /**
     * Vérifie si le store peut utiliser plus de bande passante selon son plan
     */
    private void checkBandwidthLimit(Store store, SubscriptionPlan plan, int amountInMB) {
        int currentUsage = usageMetricService.getMetricValue(store, MetricType.BANDWIDTH_USAGE);
        
        // La limite de bande passante pourrait être stockée dans les fonctionnalités du plan
        // Ici, on utilise une valeur par défaut
        int maxAllowed = 5000; // Valeur fictive en MB
        
        if (currentUsage + amountInMB > maxAllowed) {
            throw new SubscriptionLimitExceededException(
                "Bandwidth limit exceeded. Please upgrade your subscription plan.",
                LimitType.BANDWIDTH, currentUsage, maxAllowed);
        }
    }
    
    /**
     * Vérifie si le plan donne accès à une fonctionnalité spécifique
     */
    private void checkFeatureAccess(SubscriptionPlan plan, String featureName) {
        if (!plan.getFeatures().contains(featureName)) {
            throw new SubscriptionLimitExceededException(
                "This feature is not available with your current subscription plan: " + featureName,
                LimitType.FEATURE);
        }
    }
    
    /**
     * Met à jour le compteur de ressources après l'opération
     */
    private void updateMetric(Store store, LimitType limitType, int amount) {
        MetricType metricType;
        
        switch (limitType) {
            case PRODUCT:
                metricType = MetricType.PRODUCT_COUNT;
                break;
            case API_CALL:
                metricType = MetricType.API_CALLS;
                break;
            case STORAGE:
                metricType = MetricType.STORAGE_USAGE;
                break;
            case BANDWIDTH:
                metricType = MetricType.BANDWIDTH_USAGE;
                break;
            default:
                return; // Ne pas mettre à jour pour les autres types
        }
        
        usageMetricService.incrementMetric(store, metricType, amount);
    }
}
