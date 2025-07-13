# Analytics Service

Ce package contient les services et composants nécessaires pour analyser et rapporter les métriques liées aux abonnements de la plateforme ShopX.

## Objectif

Le `AnalyticsService` est responsable de la collecte, du traitement et de la présentation des données analytiques liées aux abonnements, incluant :

- Suivi des conversions de période d'essai en abonnements payants
- Analyse des revenus par plan et par période
- Suivi des taux de rétention et d'attrition des abonnements
- Mesure de l'utilisation des ressources par les stores
- Génération de rapports sur les performances du système d'abonnements

## Architecture

Le package est organisé comme suit :

- **AnalyticsService** : Interface principale définissant les opérations d'analyse
- **SubscriptionAnalyticsService** : Analyse spécifique aux abonnements
- **UsageMetricsService** : Suivi et analyse des métriques d'utilisation
- **RevenueAnalyticsService** : Analyse des revenus et de la facturation
- **ReportGenerator** : Génération de rapports formatés
- **AnalyticsScheduler** : Tâches planifiées pour la collecte et l'agrégation des données

## Fonctionnalités principales

1. **Analyse des abonnements**
   - Taux de conversion des essais gratuits
   - Durée moyenne des abonnements
   - Taux de renouvellement automatique
   - Distribution des abonnements par plan

2. **Analyse de l'utilisation**
   - Suivi de l'utilisation des produits par rapport aux limites
   - Identification des stores approchant leurs limites
   - Patterns d'utilisation des fonctionnalités par plan

3. **Analyse financière**
   - Revenu mensuel récurrent (MRR)
   - Valeur à vie des clients (LTV)
   - Prévisions de revenus
   - Analyse des impayés et des retards de paiement

4. **Rapports et visualisations**
   - Rapports périodiques (quotidiens, hebdomadaires, mensuels)
   - Tableaux de bord interactifs
   - Alertes basées sur des indicateurs clés

## Intégration

Le service d'analyse s'intègre avec les autres composants du système :

- **Subscription Service** : Pour accéder aux données d'abonnement
- **Usage Metrics Service** : Pour collecter les métriques d'utilisation
- **Invoice Service** : Pour accéder aux données financières

## Exemple d'utilisation

```java
// Obtenir le taux de conversion des essais gratuits
double conversionRate = analyticsService.getTrialConversionRate(startDate, endDate);

// Générer un rapport mensuel des revenus
RevenueReport report = analyticsService.generateMonthlyRevenueReport(year, month);

// Obtenir les stores approchant leurs limites de produits
List<StoreUsage> nearLimitStores = analyticsService.findStoresNearProductLimit(0.8); // 80% de la limite
```

## Types de rapports disponibles

Le service peut générer divers types de rapports, notamment :

- **Rapport de conversion des essais** : Analyse des transitions de l'essai à l'abonnement payant
- **Rapport de revenus** : Analyse des revenus par plan, période, et prévisions
- **Rapport d'utilisation** : Analyse de l'utilisation des ressources par les stores
- **Rapport de rétention** : Analyse des taux de rétention et d'attrition des abonnements
- **Rapport de croissance** : Analyse de la croissance des abonnements au fil du temps
