package com.olatech.shopxauthservice.Service.subscriptions;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.subscriptions.MetricType;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionPlan;
import com.olatech.shopxauthservice.Model.subscriptions.UsageMetric;
import com.olatech.shopxauthservice.Repository.subscriptions.UsageMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implémentation du service de gestion des métriques d'utilisation
 */
@Service
public class UsageMetricServiceImpl implements UsageMetricService {

    private final UsageMetricRepository metricRepository;
    private final SubscriptionService subscriptionService;

    @Autowired
    public UsageMetricServiceImpl(
            UsageMetricRepository metricRepository,
            @Lazy SubscriptionService subscriptionService) {  // Ajout de @Lazy ici pour briser la dépendance circulaire
        this.metricRepository = metricRepository;
        this.subscriptionService = subscriptionService;
    }

    @Override
    @Transactional
    public UsageMetric updateMetric(Store store, MetricType metricType, int count) {
        // Vérification des paramètres
        if (store == null) {
            throw new IllegalArgumentException("Store cannot be null");
        }
        
        if (metricType == null) {
            throw new IllegalArgumentException("Metric type cannot be null");
        }
        
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        
        // Récupération ou création de la métrique
        Optional<UsageMetric> existingMetric = metricRepository.findByStoreAndMetricType(store, metricType);
        
        UsageMetric metric;
        if (existingMetric.isPresent()) {
            metric = existingMetric.get();
            metric.setCount(count);
            metric.setLastUpdated(LocalDateTime.now());
        } else {
            metric = new UsageMetric(store, metricType, count);
        }
        
        return metricRepository.save(metric);
    }

    @Override
    @Transactional
    public UsageMetric incrementMetric(Store store, MetricType metricType, int amount) {
        // Vérification des paramètres
        if (store == null) {
            throw new IllegalArgumentException("Store cannot be null");
        }
        
        if (metricType == null) {
            throw new IllegalArgumentException("Metric type cannot be null");
        }
        
        if (amount < 0) {
            throw new IllegalArgumentException("Increment amount cannot be negative");
        }
        
        // Récupération ou création de la métrique
        Optional<UsageMetric> existingMetric = metricRepository.findByStoreAndMetricType(store, metricType);
        
        UsageMetric metric;
        if (existingMetric.isPresent()) {
            metric = existingMetric.get();
            metric.incrementCount(amount);
        } else {
            metric = new UsageMetric(store, metricType, amount);
        }
        
        return metricRepository.save(metric);
    }

    @Override
    @Transactional
    public UsageMetric decrementMetric(Store store, MetricType metricType, int amount) {
        // Vérification des paramètres
        if (store == null) {
            throw new IllegalArgumentException("Store cannot be null");
        }
        
        if (metricType == null) {
            throw new IllegalArgumentException("Metric type cannot be null");
        }
        
        if (amount < 0) {
            throw new IllegalArgumentException("Decrement amount cannot be negative");
        }
        
        // Récupération ou création de la métrique
        Optional<UsageMetric> existingMetric = metricRepository.findByStoreAndMetricType(store, metricType);
        
        UsageMetric metric;
        if (existingMetric.isPresent()) {
            metric = existingMetric.get();
            metric.decrementCount(amount);
        } else {
            metric = new UsageMetric(store, metricType, 0);
        }
        
        return metricRepository.save(metric);
    }

    @Override
    public Optional<UsageMetric> findMetric(Store store, MetricType metricType) {
        return metricRepository.findByStoreAndMetricType(store, metricType);
    }

    @Override
    public List<UsageMetric> findAllMetrics(Store store) {
        return metricRepository.findByStore(store);
    }

    @Override
    public int getMetricValue(Store store, MetricType metricType) {
        Optional<UsageMetric> metric = metricRepository.findByStoreAndMetricType(store, metricType);
        return metric.map(UsageMetric::getCount).orElse(0);
    }

    @Override
    public boolean isMetricOverThreshold(Store store, MetricType metricType, int threshold) {
        int currentValue = getMetricValue(store, metricType);
        return currentValue >= threshold;
    }

    @Override
    public double getUsagePercentage(Store store, MetricType metricType, int limit) {
        if (limit <= 0) {
            return 1.0; // 100% d'utilisation si la limite est 0 ou négative
        }
        
        int currentValue = getMetricValue(store, metricType);
        return (double) currentValue / limit;
    }

    @Override
    public Map<Store, Double> findStoresNearLimit(MetricType metricType, double thresholdPercentage) {
        // Récupère toutes les métriques du type spécifié
        List<UsageMetric> metrics = metricRepository.findByMetricType(metricType);
        
        Map<Store, Double> result = new HashMap<>();
        
        for (UsageMetric metric : metrics) {
            Store store = metric.getStore();
            
            // Récupère l'abonnement actif du store
            subscriptionService.findActiveSubscription(store).ifPresent(subscription -> {
                SubscriptionPlan plan = subscription.getPlan();
                int limit = 0;
                
                // Déterminer la limite en fonction du type de métrique
                if (metricType == MetricType.PRODUCT_COUNT) {
                    limit = plan.getMaxProducts();
                } else if (metricType == MetricType.API_CALLS) {
                    // Supposons que la limite est stockée dans les fonctionnalités du plan
                    // Ceci est un exemple simplifié
                    limit = 10000; // Valeur par défaut
                } else if (metricType == MetricType.STORAGE_USAGE) {
                    // Similarité
                    limit = 1000; // Valeur par défaut en MB
                }
                
                if (limit > 0) {
                    double percentage = (double) metric.getCount() / limit;
                    
                    // Ajouter au résultat si le seuil est atteint
                    if (percentage >= thresholdPercentage) {
                        result.put(store, percentage);
                    }
                }
            });
        }
        
        return result;
    }

    @Override
    @Transactional
    public UsageMetric resetMetric(Store store, MetricType metricType) {
        Optional<UsageMetric> existingMetric = metricRepository.findByStoreAndMetricType(store, metricType);
        
        UsageMetric metric;
        if (existingMetric.isPresent()) {
            metric = existingMetric.get();
            metric.setCount(0);
            metric.setLastUpdated(LocalDateTime.now());
        } else {
            metric = new UsageMetric(store, metricType, 0);
        }
        
        return metricRepository.save(metric);
    }
}
