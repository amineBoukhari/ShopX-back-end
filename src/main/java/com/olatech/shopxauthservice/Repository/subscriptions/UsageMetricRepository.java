package com.olatech.shopxauthservice.Repository.subscriptions;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.subscriptions.MetricType;
import com.olatech.shopxauthservice.Model.subscriptions.UsageMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsageMetricRepository extends JpaRepository<UsageMetric, Long> {
    
    /**
     * Trouve une métrique spécifique pour un store
     */
    Optional<UsageMetric> findByStoreAndMetricType(Store store, MetricType metricType);
    
    /**
     * Trouve toutes les métriques d'un store
     */
    List<UsageMetric> findByStore(Store store);
    
    /**
     * Trouve toutes les métriques par type
     */
    List<UsageMetric> findByMetricType(MetricType metricType);
    
    /**
     * Trouve les métriques qui dépassent un certain seuil
     */
    @Query("SELECT m FROM UsageMetric m WHERE m.metricType = :metricType AND m.count >= :threshold")
    List<UsageMetric> findByMetricTypeAndCountGreaterThanEqual(MetricType metricType, int threshold);
    
    /**
     * Trouve les métriques mises à jour depuis une date
     */
    List<UsageMetric> findByLastUpdatedAfter(LocalDateTime date);
    
    /**
     * Calcule la moyenne d'une métrique pour tous les stores
     */
    @Query("SELECT AVG(m.count) FROM UsageMetric m WHERE m.metricType = :metricType")
    Double calculateAverageCount(MetricType metricType);
    
    /**
     * Calcule la valeur maximale d'une métrique
     */
    @Query("SELECT MAX(m.count) FROM UsageMetric m WHERE m.metricType = :metricType")
    Integer findMaxCount(MetricType metricType);
}
