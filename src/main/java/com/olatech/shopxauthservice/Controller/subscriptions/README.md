# API de gestion des abonnements (Subscription Management API)

Ce document décrit l'ensemble des endpoints disponibles pour gérer les abonnements à la plateforme depuis une interface frontend.

## Plans d'abonnement

Ces endpoints permettent de récupérer des informations sur les plans d'abonnement disponibles.

### Obtenir tous les plans actifs

```
GET /api/subscriptions/plans
```

**Description** : Retourne la liste de tous les plans d'abonnement actifs avec leurs détails (prix, fonctionnalités, etc.)

**Réponse** : `200 OK` avec un tableau d'objets de plans

### Obtenir les détails d'un plan spécifique

```
GET /api/subscriptions/plans/{planId}
```

**Description** : Retourne les détails complets d'un plan d'abonnement spécifique

**Paramètres de chemin** :
- `planId` : ID du plan d'abonnement

**Réponse** : `200 OK` avec les détails du plan ou `404 Not Found` si le plan n'existe pas

## Abonnements de store

Ces endpoints permettent de gérer les abonnements des stores.

### Obtenir l'abonnement actif d'un store

```
GET /api/subscriptions/stores/{storeId}/active
```

**Description** : Retourne les détails de l'abonnement actif d'un store

**Paramètres de chemin** :
- `storeId` : ID du store

**Réponse** : `200 OK` avec les détails de l'abonnement ou `404 Not Found` si aucun abonnement actif n'existe

### Obtenir l'historique des abonnements d'un store

```
GET /api/subscriptions/stores/{storeId}/history
```

**Description** : Retourne l'historique complet des abonnements d'un store (abonnements passés et actuel)

**Paramètres de chemin** :
- `storeId` : ID du store

**Réponse** : `200 OK` avec un tableau d'objets d'abonnement

### S'abonner à un plan (avec période d'essai)

```
POST /api/subscriptions/stores/{storeId}/subscribe
```

**Description** : Crée un nouvel abonnement pour un store avec une période d'essai si applicable

**Paramètres de chemin** :
- `storeId` : ID du store

**Corps de la requête** :
```json
{
  "planId": 2,
  "billingCycle": "MONTHLY"
}
```

**Réponse** : `201 Created` avec les détails du nouvel abonnement ou `400 Bad Request` si le plan n'existe pas

### Changer de plan d'abonnement

```
PUT /api/subscriptions/stores/{storeId}/change-plan
```

**Description** : Change le plan d'abonnement d'un store

**Paramètres de chemin** :
- `storeId` : ID du store

**Corps de la requête** :
```json
{
  "newPlanId": 3,
  "billingCycle": "YEARLY"
}
```

**Réponse** : `200 OK` avec les détails de l'abonnement mis à jour ou `404 Not Found` si aucun abonnement actif n'existe

### Annuler un abonnement

```
POST /api/subscriptions/stores/{storeId}/cancel
```

**Description** : Annule l'abonnement d'un store (immédiatement ou à la fin de la période de facturation)

**Paramètres de chemin** :
- `storeId` : ID du store

**Corps de la requête** :
```json
{
  "cancelImmediately": false,
  "reason": "Switching to another service"
}
```

**Réponse** : `200 OK` avec les détails de l'abonnement annulé ou `404 Not Found` si aucun abonnement actif n'existe

### Activer/Désactiver le renouvellement automatique

```
PUT /api/subscriptions/stores/{storeId}/auto-renew
```

**Description** : Active ou désactive le renouvellement automatique d'un abonnement

**Paramètres de chemin** :
- `storeId` : ID du store

**Corps de la requête** :
```json
{
  "autoRenew": true
}
```

**Réponse** : `200 OK` avec les détails de l'abonnement mis à jour ou `404 Not Found` si aucun abonnement actif n'existe

## Factures

Ces endpoints permettent de gérer les factures liées aux abonnements.

### Obtenir toutes les factures d'un store

```
GET /api/subscriptions/stores/{storeId}/invoices
```

**Description** : Retourne la liste de toutes les factures d'un store

**Paramètres de chemin** :
- `storeId` : ID du store

**Réponse** : `200 OK` avec un tableau d'objets de facture

### Obtenir les détails d'une facture spécifique

```
GET /api/subscriptions/invoices/{invoiceId}
```

**Description** : Retourne les détails complets d'une facture spécifique

**Paramètres de chemin** :
- `invoiceId` : ID de la facture

**Réponse** : `200 OK` avec les détails de la facture ou `404 Not Found` si la facture n'existe pas

### Payer une facture

```
POST /api/subscriptions/invoices/{invoiceId}/pay
```

**Description** : Marque une facture comme payée (simulation de paiement)

**Paramètres de chemin** :
- `invoiceId` : ID de la facture

**Corps de la requête** :
```json
{
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "txn_123456789"
}
```

**Réponse** : `200 OK` avec les détails de la facture payée ou `404 Not Found` si la facture n'existe pas

## Métriques d'utilisation

Ces endpoints permettent de suivre l'utilisation des ressources par rapport aux limites du plan.

### Obtenir les métriques d'utilisation d'un store

```
GET /api/subscriptions/stores/{storeId}/usage
```

**Description** : Retourne les métriques d'utilisation actuelles d'un store (produits, appels API, stockage, etc.)

**Paramètres de chemin** :
- `storeId` : ID du store

**Réponse** : `200 OK` avec un objet contenant les métriques d'utilisation ou `404 Not Found` si le store n'existe pas

Exemple de réponse :
```json
{
  "productCount": 45,
  "maxProducts": 100,
  "productUsagePercent": 45.0,
  "apiCalls": 1250,
  "storageUsage": 350
}
```

## Intégration avec une interface frontend

Pour intégrer ces endpoints dans une application frontend, vous devrez :

1. **Afficher les plans disponibles** sur une page dédiée, avec comparaison des fonctionnalités
2. **Implémenter un processus d'abonnement** permettant aux utilisateurs de choisir un plan et un cycle de facturation
3. **Créer un tableau de bord d'abonnement** montrant :
   - Le plan actuel et ses limites
   - Les métriques d'utilisation (avec visualisations)
   - L'historique des factures
   - Options pour changer de plan, annuler l'abonnement, etc.
4. **Mettre en place des notifications** pour informer les utilisateurs lorsqu'ils approchent des limites de leur plan

## Gestion des erreurs

Les erreurs courantes que votre frontend doit gérer incluent :

- `404 Not Found` : Ressource non trouvée (plan, abonnement, facture, etc.)
- `400 Bad Request` : Requête invalide (données manquantes ou incorrectes)
- `402 Payment Required` : Limite du plan d'abonnement atteinte
- `403 Forbidden` : Accès refusé (tentative d'accès à une ressource d'un autre utilisateur)

## Sécurité

Tous les endpoints nécessitent une authentification. Assurez-vous que votre frontend inclut les tokens JWT appropriés dans les en-têtes de requête.
