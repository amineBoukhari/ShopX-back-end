# Guide de Configuration OAuth2 pour ShopX

## Introduction

Ce document fournit les instructions détaillées pour configurer les intégrations OAuth2 avec Google et Facebook pour l'application ShopX. La configuration correcte de ces intégrations est essentielle pour assurer un processus d'authentification sécurisé.

## Sommaire

1. [Prérequis](#prérequis)
2. [Configuration de Google OAuth2](#configuration-de-google-oauth2)
3. [Configuration de Facebook OAuth2](#configuration-de-facebook-oauth2)
4. [Configuration des Environnements](#configuration-des-environnements)
5. [Test et Validation](#test-et-validation)
6. [Résolution des Problèmes Courants](#résolution-des-problèmes-courants)

## Prérequis

Avant de commencer, assurez-vous de disposer des éléments suivants :

- Accès à la [Console Google Cloud](https://console.cloud.google.com/)
- Accès au [Tableau de bord des développeurs Facebook](https://developers.facebook.com/)
- Accès aux serveurs de déploiement (développement, test, production)
- Connaissance des domaines/URLs de redirection de votre application

## Configuration de Google OAuth2

### Création du Projet Google Cloud

1. Connectez-vous à la [Console Google Cloud](https://console.cloud.google.com/)
2. Créez un nouveau projet ou sélectionnez un projet existant
3. Notez l'ID du projet qui sera utilisé dans les étapes suivantes

### Configuration OAuth2 pour Google

1. Dans le menu de navigation, sélectionnez "APIs & Services" > "Credentials"
2. Cliquez sur "Create Credentials" et sélectionnez "OAuth client ID"
3. Configurez l'écran de consentement OAuth :
   - Type d'application : Web application
   - Nom : "ShopX Authentication"
   - Domaines autorisés : Ajoutez vos domaines (ex: `olatechsn.com`)
   - Emails de contact : Ajoutez l'email de l'administrateur

4. Configurez les identifiants OAuth2 :
   - Type d'application : Application Web
   - Nom : "ShopX Web Client"
   - URLs JavaScript autorisées : 
     ```
     https://app-shopx.olatechsn.com
     https://shopx.olatechsn.com
     http://localhost:3000  # Pour le développement local
     ```
   - URLs de redirection autorisées :
     ```
     https://api-shopx.olatechsn.com/oauth2/callback/google
     http://localhost:8080/oauth2/callback/google  # Pour le développement local
     ```

5. Cliquez sur "Create"
6. Notez le "Client ID" et le "Client Secret" qui seront utilisés dans la configuration de l'application

### Activation des APIs Nécessaires

1. Dans le menu de navigation, sélectionnez "APIs & Services" > "Library"
2. Activez les APIs suivantes :
   - Google+ API
   - People API
   - Google Identity Toolkit API

## Configuration de Facebook OAuth2

### Création de l'Application Facebook

1. Connectez-vous au [Tableau de bord des développeurs Facebook](https://developers.facebook.com/)
2. Cliquez sur "Créer une application"
3. Sélectionnez "Type d'application" : "Grand public"
4. Remplissez les informations demandées et cliquez sur "Créer une application"

### Configuration de l'Application Facebook

1. Dans le menu de navigation, sélectionnez "Paramètres" > "De base"
2. Notez l'ID de l'application et la clé secrète qui seront utilisés dans la configuration
3. Ajoutez les domaines de votre application dans "Domaines de l'application"
4. Allez dans "Facebook Login" > "Paramètres"
5. Configurez les URL de redirection OAuth valides :
   ```
   https://api-shopx.olatechsn.com/oauth2/callback/facebook
   http://localhost:8080/oauth2/callback/facebook  # Pour le développement local
   ```
6. Configurez les paramètres d'expérience de connexion :
   - Activer "Connexion par déconnexion"
   - Activer "Utiliser le code d'autorisation strict"
   - Activer "Imposer le protocole HTTPS"
   - Définir "L'URI de redirection d'erreur de connexion OAuth"

7. Dans "Permissions et fonctionnalités", ajoutez les permissions suivantes :
   - email
   - public_profile

8. Passez votre application en mode "Live" si vous souhaitez l'utiliser en production

## Configuration des Environnements

### Configuration du Backend (Spring Boot)

1. Ouvrez le fichier `application-oauth2.properties` et configurez les valeurs suivantes :

```properties
# Configuration OAuth2 pour Google
oauth2.client-registrations.google.client-id=<VOTRE_CLIENT_ID_GOOGLE>
oauth2.client-registrations.google.client-secret=<VOTRE_CLIENT_SECRET_GOOGLE>
oauth2.client-registrations.google.redirect-uri=https://api-shopx.olatechsn.com/oauth2/callback/google

# Configuration OAuth2 pour Facebook
oauth2.client-registrations.facebook.client-id=<VOTRE_CLIENT_ID_FACEBOOK>
oauth2.client-registrations.facebook.client-secret=<VOTRE_CLIENT_SECRET_FACEBOOK>
oauth2.client-registrations.facebook.redirect-uri=https://api-shopx.olatechsn.com/oauth2/callback/facebook

# Configuration générale OAuth2
oauth2.authorized-redirect-uris=https://app-shopx.olatechsn.com/app,https://shopx.olatechsn.com/app,http://localhost:3000/app,https://front-core-service-389976410269.europe-west1.run.app
oauth2.default-redirect-uri=https://app-shopx.olatechsn.com/app

# Configuration des cookies
cookie.domain=olatechsn.com
cookie.secure=true

# Configuration JWT 
jwt.secret=<VOTRE_SECRET_JWT_ALÉATOIRE>
jwt.access-token.expiration=3600000
jwt.refresh-token.expiration=604800000
```

2. Vous pouvez également utiliser des variables d'environnement pour les valeurs sensibles. Dans ce cas, assurez-vous que les variables d'environnement sont correctement définies sur le serveur de déploiement.

### Configuration du Frontend (Vue/Nuxt)

La configuration côté client est simplifiée car les boutons d'authentification sociale font simplement appel aux endpoints du backend. Cependant, vous pouvez configurer les URLs d'API dans votre application frontend :

```js
// Dans votre fichier de configuration nuxt.config.js
export default {
  publicRuntimeConfig: {
    apiBaseUrl: process.env.API_BASE_URL || 'https://api-shopx.olatechsn.com',
    appBaseUrl: process.env.APP_BASE_URL || 'https://app-shopx.olatechsn.com'
  }
}
```

## Test et Validation

Après avoir configuré les intégrations OAuth2, vous devriez valider que tout fonctionne correctement :

1. Testez la connexion avec Google : 
   - Cliquez sur le bouton "Se connecter avec Google"
   - Vérifiez que vous êtes redirigé vers Google pour l'autorisation
   - Vérifiez que vous êtes redirigé vers l'application après autorisation
   - Vérifiez que vous êtes correctement authentifié

2. Testez la connexion avec Facebook (même processus)

3. Vérifiez les journaux d'application pour vous assurer qu'aucune erreur n'est signalée

4. Vérifiez la base de données pour vous assurer que les informations utilisateur sont correctement stockées

## Résolution des Problèmes Courants

### Problème : Erreur de redirection

**Symptôme :** Après l'authentification OAuth2, vous obtenez une erreur "redirect_uri_mismatch".

**Solution :** 
- Vérifiez que les URLs de redirection configurées dans la console Google Cloud ou Facebook correspondent exactement à celles configurées dans votre application.
- Assurez-vous que le protocole (http vs https) est le même.
- Vérifiez que le port est inclus si nécessaire (pour localhost).

### Problème : State mismatch

**Symptôme :** Erreur "state_mismatch" ou "Invalid state parameter".

**Solution :** 
- Vérifiez que vous utilisez correctement le paramètre "state".
- Vérifiez que les sessions sont correctement configurées.
- Vérifiez que vous n'avez pas de problèmes de cookies entre domaines.

### Problème : Informations utilisateur manquantes

**Symptôme :** L'authentification réussit mais certaines informations utilisateur (email, nom) sont manquantes.

**Solution :** 
- Vérifiez que vous avez demandé les bonnes permissions (scopes) dans la configuration OAuth2.
- Pour Facebook, assurez-vous que les permissions email et public_profile sont correctement configurées.
- Pour Google, assurez-vous que vous avez activé les APIs nécessaires.

## Support et Contact

Pour toute question ou problème concernant la configuration OAuth2, veuillez contacter l'équipe de développement à `support@olatech.sn`.

---

Document mis à jour le 12 mars 2025
