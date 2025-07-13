# Notification Service

Ce package contient les services et composants nécessaires pour gérer les notifications du système d'abonnements de la plateforme ShopX.

## Objectif

Le `NotificationService` est responsable de l'envoi de notifications aux utilisateurs concernant les événements liés aux abonnements, notamment :

- Notifications de bienvenue lors de l'inscription à un plan
- Rappels de fin de période d'essai
- Notifications de renouvellement d'abonnement imminent
- Confirmations de paiement
- Alertes de factures impayées
- Notifications d'expiration d'abonnement
- Alertes d'approche des limites du plan (ex: 80% des produits utilisés)

## Architecture

Le package est organisé comme suit :

- **NotificationService** : Interface principale définissant les opérations d'envoi de notifications
- **EmailNotificationService** : Implémentation pour l'envoi de notifications par email
- **NotificationTemplate** : Modèles de notifications
- **NotificationHistory** : Suivi des notifications envoyées
- **NotificationScheduler** : Tâches planifiées pour l'envoi automatique de notifications

## Fonctionnalités principales

1. **Gestion des notifications d'abonnement**
   - Envoi de notifications lors des changements d'état d'abonnement
   - Rappels automatiques pour les dates importantes
   - Suivi des notifications envoyées

2. **Personnalisation des notifications**
   - Templates personnalisables selon le type de notification
   - Inclusion de données spécifiques au store et à l'abonnement
   - Support multilingue

3. **Canaux de notification**
   - Notification par email (implémentation principale)
   - Extensible pour d'autres canaux (SMS, notifications in-app)

4. **Suivi et historique**
   - Enregistrement des notifications envoyées
   - Vérification des notifications lues/non lues
   - Statistiques sur l'efficacité des notifications

## Intégration

Le service de notification s'intègre avec les autres composants du système :

- **Subscription Service** : Pour accéder aux données d'abonnement et aux événements
- **User Service** : Pour obtenir les informations de contact des utilisateurs
- **Email Service** : Pour l'envoi d'emails via les serveurs SMTP configurés

## Exemple d'utilisation

```java
// Envoyer une notification de bienvenue
notificationService.sendWelcomeNotification(user, subscription);

// Envoyer un rappel de fin de période d'essai
notificationService.sendTrialEndingReminder(subscription);

// Envoyer une notification de renouvellement imminent
notificationService.sendRenewalReminder(subscription, 3); // 3 jours avant
```

## Modèles de notification

Le service inclut des modèles prédéfinis pour les types de notifications courants :

- **welcome_subscription.html** : Message de bienvenue pour un nouvel abonnement
- **trial_ending.html** : Rappel de fin de période d'essai
- **renewal_reminder.html** : Rappel de renouvellement imminent
- **payment_confirmation.html** : Confirmation de paiement
- **subscription_expired.html** : Notification d'expiration d'abonnement
- **limit_approaching.html** : Alerte d'approche des limites du plan
