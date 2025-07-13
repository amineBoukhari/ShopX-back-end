package com.olatech.shopxauthservice.Service.subscriptions;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.subscriptions.MetricType;
import com.olatech.shopxauthservice.Model.subscriptions.UsageMetric;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service dédié à la gestion des métriques d'utilisation
 */
public interface UsageMetricService {

    /**
     * Crée ou met à jour une métrique d'utilisation
     * @param store Le store concerné
     * @param metricType Type de métrique
     * @param count Valeur du compteur
     * @return La métrique mise à jour
     */
    UsageMetric updateMetric(Store store, MetricType metricType, int count);

    /**
     * Incrémente une métrique d'utilisation
     * @param store Le store concerné
     * @param metricType Type de métrique
     * @param amount Quantité à ajouter
     * @return La métrique mise à jour
     */
    UsageMetric incrementMetric(Store store, MetricType metricType, int amount);

    /**
     * Décrémente une métrique d'utilisation
     * @param store Le store concerné
     * @param metricType Type de métrique
     * @param amount Quantité à soustraire
     * @return La métrique mise à jour
     */
    UsageMetric decrementMetric(Store store, MetricType metricType, int amount);

    /**
     * Récupère une métrique spécifique pour un store
     * @param store Le store
     * @param metricType Type de métrique
     * @return La métrique si elle existe
     */
    Optional<UsageMetric> findMetric(Store store, MetricType metricType);

    /**
     * Récupère toutes les métriques d'un store
     * @param store Le store
     * @return Liste des métriques
     */
    List<UsageMetric> findAllMetrics(Store store);

    /**
     * Récupère la valeur d'une métrique
     * @param store Le store
     * @param metricType Type de métrique
     * @return Valeur de la métrique ou 0 si non trouvée
     */
    int getMetricValue(Store store, MetricType metricType);

    /**
     * Vérifie si une métrique dépasse un certain seuil
     * @param store Le store
     * @param metricType Type de métrique
     * @param threshold Seuil à vérifier
     * @return true si la métrique dépasse le seuil, false sinon
     */
    boolean isMetricOverThreshold(Store store, MetricType metricType, int threshold);

    /**
     * Calcule le pourcentage d'utilisation d'une métrique par rapport à une limite
     * @param store Le store
     * @param metricType Type de métrique
     * @param limit Limite maximale
     * @return Pourcentage d'utilisation (0.0 à 1.0)
     */
    double getUsagePercentage(Store store, MetricType metricType, int limit);

    /**
     * Récupère les stores qui sont proches d'une limite pour une métrique spécifique
     * @param metricType Type de métrique
     * @param thresholdPercentage Pourcentage seuil (ex: 0.8 pour 80%)
     * @return Map des stores avec leur pourcentage d'utilisation
     */
    Map<Store, Double> findStoresNearLimit(MetricType metricType, double thresholdPercentage);

    /**
     * Réinitialise une métrique spécifique
     * @param store Le store
     * @param metricType Type de métrique
     * @return La métrique réinitialisée
     */
    UsageMetric resetMetric(Store store, MetricType metricType);
}
