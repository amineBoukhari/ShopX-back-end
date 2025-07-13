# Subscription Service

Ce package contient les services et les composants nécessaires pour gérer le système d'abonnements de la plateforme ShopX.

## Objectif

Le `SubscriptionService` est responsable de la gestion du cycle de vie complet des abonnements des stores, incluant :

- La création et l'activation d'abonnements
- La gestion des périodes d'essai
- Le renouvellement automatique des abonnements
- La facturation des abonnements
- La gestion des changements de plan (upgrade/downgrade)
- La vérification des limites et des restrictions basées sur le plan d'abonnement

## Architecture

Le package est organisé comme suit :

- **SubscriptionService** : Interface principale définissant les opérations de gestion des abonnements
- **SubscriptionServiceImpl** : Implémentation de l'interface SubscriptionService
- **SubscriptionPlanService** : Gestion des plans d'abonnement (création, mise à jour, etc.)
- **SubscriptionInvoiceService** : Génération et gestion des factures
- **SubscriptionValidator** : Validations des opérations liées aux abonnements
- **SubscriptionScheduler** : Tâches planifiées pour la gestion automatique des abonnements (renouvellements, expirations)

## Fonctionnalités principales

1. **Gestion des abonnements**
   - Création d'un nouvel abonnement avec période d'essai
   - Activation et désactivation d'abonnements
   - Renouvellement automatique
   - Changement de plan d'abonnement

2. **Facturation**
   - Génération automatique de factures
   - Suivi des paiements
   - Gestion des retards de paiement

3. **Vérification des limites**
   - Contrôle du nombre de produits en fonction du plan
   - Vérification des fonctionnalités accessibles
   - Gestion des dépassements de limites

4. **Workflow d'abonnement**
   - Transition entre les différents états (essai → actif → expiré/annulé)
   - Gestion des dates clés (début, fin, prochain renouvellement)

## Intégration

Le service d'abonnement s'intègre avec les autres composants du système :

- **Notification Service** : Pour envoyer des notifications liées aux abonnements
- **Analytics Service** : Pour suivre et analyser les métriques d'utilisation
- **Product Service** : Pour vérifier et appliquer les limites de produits

## Exemple d'utilisation

```java
// Créer un nouvel abonnement avec période d'essai
subscriptionService.createTrialSubscription(store, subscriptionPlan, BillingCycle.MONTHLY);

// Vérifier si un store peut ajouter un produit
boolean canAddProduct = subscriptionService.canAddProduct(store);

// Changer de plan d'abonnement
subscriptionService.changePlan(storeSubscription, newPlan, BillingCycle.YEARLY);
```
