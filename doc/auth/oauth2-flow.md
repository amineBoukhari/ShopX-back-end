# Documentation du Flux d'Authentification OAuth2

## Vue d'ensemble

Ce document décrit le flux d'authentification OAuth2 mis en œuvre dans l'application ShopX, incluant les intégrations avec Google et Facebook, ainsi que les mesures de sécurité implémentées.

## Table des matières

1. [Architecture globale](#architecture-globale)
2. [Flux d'authentification OAuth2](#flux-dauthentification-oauth2)
3. [Mesures de sécurité](#mesures-de-sécurité)
4. [Gestion des tokens](#gestion-des-tokens)
5. [Processus de déconnexion](#processus-de-déconnexion)
6. [Structure des entités](#structure-des-entités)
7. [Points d'API](#points-dapi)

## Architecture globale

Le système d'authentification ShopX s'appuie sur une architecture client-serveur sécurisée :

- **Backend (Java Spring)** : Service `shopx-auth-service` gérant l'authentification, les tokens et les sessions.
- **Frontend (Vue.js/Nuxt)** : Application `shopx-v4` consommant les API d'authentification et stockant les tokens.

L'authentification peut se faire via :
- Credentials locaux (email/mot de passe)
- OAuth2 avec Google
- OAuth2 avec Facebook

## Flux d'authentification OAuth2

### Initialisation de l'authentification

1. **Demande d'authentification** : L'utilisateur clique sur un bouton de connexion sociale (Google ou Facebook).
2. **Génération des paramètres de sécurité** :
   - Génération d'un token `state` aléatoire pour prévenir les attaques CSRF
   - Pour Google, génération d'un code challenge PKCE
   - Stockage de ces paramètres en session côté serveur
3. **Redirection vers le fournisseur** : L'utilisateur est redirigé vers le fournisseur OAuth2 (Google/Facebook) avec les paramètres sécurisés.

### Callback et traitement

1. **Réception du callback** : Le fournisseur redirige l'utilisateur vers notre URL de callback avec un code d'autorisation.
2. **Validation de sécurité** :
   - Vérification du paramètre `state` pour prévenir les attaques CSRF
   - Validation de l'URL de redirection contre une liste blanche
3. **Échange du code** : Le code d'autorisation est échangé contre un token d'accès auprès du fournisseur OAuth2.
4. **Récupération des informations utilisateur** : Utilisation du token pour obtenir les informations de profil de l'utilisateur.
5. **Création ou mise à jour du compte** :
   - Si l'utilisateur existe (par email), mise à jour des informations
   - Si l'utilisateur n'existe pas, création d'un nouveau compte
6. **Génération des tokens JWT** :
   - Génération d'un token d'accès JWT (durée : 1 heure)
   - Génération d'un token de rafraîchissement JWT (durée : 7 jours)
7. **Enregistrement de la session** : Sauvegarde de la session dans la base de données avec références aux tokens.
8. **Transmission des tokens** : Envoi des tokens au client via des cookies sécurisés.
9. **Redirection** : L'utilisateur est redirigé vers l'application avec un paramètre indiquant le succès de l'authentification.

### Diagramme de séquence

```
┌─────────┐          ┌─────────┐          ┌──────────┐          ┌─────────┐
│ Frontend │          │ Backend │          │ Provider │          │ Database│
└────┬────┘          └────┬────┘          └─────┬────┘          └────┬────┘
     │                     │                     │                    │
     │  Init Auth Request  │                     │                    │
     │ ──────────────────> │                     │                    │
     │                     │                     │                    │
     │                     │ Generate state/PKCE │                    │
     │                     │ ───────────────────┐│                    │
     │                     │                    ││                    │
     │                     │ <────────────────┘ │                    │
     │                     │                     │                    │
     │                     │ Auth URL + state    │                    │
     │ <─────────────────── │                     │                    │
     │                     │                     │                    │
     │   Redirect to      │                     │                    │
     │  Provider with     │                     │                    │
     │  state/PKCE        │                     │                    │
     │ ────────────────────────────────────────> │                    │
     │                     │                     │                    │
     │   User Authorizes   │                     │                    │
     │ <───────────────────────────────────────── │                    │
     │                     │                     │                    │
     │  Redirect to       │                     │                    │
     │  Callback with code│                     │                    │
     │  and state         │                     │                    │
     │ ──────────────────> │                     │                    │
     │                     │                     │                    │
     │                     │  Verify state      │                    │
     │                     │ ───────────────────┐│                    │
     │                     │                    ││                    │
     │                     │ <────────────────┘ │                    │
     │                     │                     │                    │
     │                     │  Exchange code     │                    │
     │                     │ ────────────────────>                    │
     │                     │                     │                    │
     │                     │  Access token      │                    │
     │                     │ <──────────────────┘                    │
     │                     │                     │                    │
     │                     │  Get user info     │                    │
     │                     │ ────────────────────>                    │
     │                     │                     │                    │
     │                     │  User profile      │                    │
     │                     │ <──────────────────┘                    │
     │                     │                     │                    │
     │                     │  Find/Create user  │                    │
     │                     │ ────────────────────────────────────────>
     │                     │                     │                    │
     │                     │  User saved        │                    │
     │                     │ <────────────────────────────────────────
     │                     │                     │                    │
     │                     │  Generate JWT      │                    │
     │                     │ ───────────────────┐│                    │
     │                     │                    ││                    │
     │                     │ <────────────────┘ │                    │
     │                     │                     │                    │
     │                     │  Save session      │                    │
     │                     │ ────────────────────────────────────────>
     │                     │                     │                    │
     │                     │  Session saved     │                    │
     │                     │ <────────────────────────────────────────
     │                     │                     │                    │
     │  Redirect + Cookies │                     │                    │
     │ <─────────────────── │                     │                    │
     │                     │                     │                    │
```

## Mesures de sécurité

Les mesures de sécurité suivantes ont été implémentées pour protéger le flux OAuth2 :

### Protection contre les attaques CSRF

- **Paramètre state** : Généré aléatoirement pour chaque demande d'authentification.
- **Stockage sécurisé** : Le paramètre `state` est stocké en session côté serveur.
- **Expiration** : Le paramètre `state` expire après 10 minutes.
- **Validation stricte** : Le paramètre `state` du callback est strictement comparé à celui stocké.
- **Usage unique** : Le paramètre `state` est supprimé après validation.

### Validation des URLs de redirection

- **Liste blanche** : Toutes les URLs de redirection sont validées contre une liste blanche configurée.
- **Validation de domaine** : Seules les URLs de domaines autorisés sont acceptées.
- **Repli sécurisé** : En cas d'URL invalide, l'utilisateur est redirigé vers une URL par défaut sécurisée.

### Protection des tokens

- **Cookies sécurisés** : Les tokens sont stockés dans des cookies avec les protections suivantes :
  - HttpOnly : Inaccessibles par JavaScript
  - Secure : Transmis uniquement via HTTPS (en production)
  - SameSite : Protection contre les attaques CSRF
  - Domain : Limité au domaine spécifique
  - Expiration : Durée de vie limitée

- **JWT sécurisés** :
  - Identifiants uniques (jti) pour chaque token permettant la révocation
  - Signatures avec algorithme HMAC-SHA256
  - Durée de vie limitée (1h pour l'accès, 7j pour le rafraîchissement)
  - Clé secrète stockée dans les variables d'environnement

### Enregistrement des sessions

- **Suivi des sessions** : Toutes les sessions actives sont enregistrées en base de données.
- **Informations enregistrées** :
  - Références aux tokens (via jti)
  - User-Agent
  - Adresse IP
  - Dates de création et d'expiration

### Sécurité PKCE (pour Google)

- **Code Challenge** : Utilisation de la méthode PKCE (Proof Key for Code Exchange) pour Google.
- **Stockage sécurisé** : Le code verifier est stocké en session côté serveur.
- **Usage unique** : Le code verifier est supprimé après utilisation.

## Gestion des tokens

### Structure des tokens JWT

**Token d'accès** :
```json
{
  "jti": "identifiant_unique_du_token",
  "sub": "username_utilisateur",
  "userId": 123,
  "stores": [
    {
      "storeId": 1,
      "role": "ADMIN"
    },
    {
      "storeId": 2,
      "role": "EDITOR"
    }
  ],
  "permissions": ["READ", "WRITE", "DELETE"],
  "iat": 1615393124,
  "exp": 1615396724
}
```

**Token de rafraîchissement** :
```json
{
  "jti": "identifiant_unique_du_token",
  "sub": "username_utilisateur",
  "type": "refresh",
  "iat": 1615393124,
  "exp": 1616002724
}
```

### Rafraîchissement des tokens

1. Le client envoie une requête avec le token de rafraîchissement.
2. Le serveur valide le token de rafraîchissement :
   - Vérification de la signature
   - Vérification de l'expiration
   - Vérification que le token n'est pas révoqué
3. Si valide, le serveur génère un nouveau token d'accès.
4. Le token d'accès est renvoyé au client.
5. Le client met à jour son stockage local.

### Rotation des tokens

Pour améliorer la sécurité, nous n'implémentons pas actuellement une rotation complète des tokens de rafraîchissement, mais cette fonctionnalité pourrait être ajoutée ultérieurement.

## Processus de déconnexion

1. **Demande de déconnexion** : L'utilisateur déclenche une déconnexion.
2. **Révocation des tokens** :
   - Le token d'accès est ajouté à la table de révocation.
   - Le token de rafraîchissement est ajouté à la table de révocation.
3. **Suppression de la session** : La session correspondante est supprimée de la base de données.
4. **Suppression des cookies** : Les cookies contenant les tokens sont supprimés côté client.
5. **Redirection** : L'utilisateur est redirigé vers la page de connexion.

## Structure des entités

### Entité Sessions

```java
@Entity
public class Sessions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Users user;
    
    private String token;
    private String refreshToken;
    private Date createdAt;
    private Date expiresAt;
    private String userAgent;
    private String ipAddress;
    
    // Getters et Setters
}
```

### Entité RevokedToken

```java
@Entity
public class RevokedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String tokenId;
    private Date expiryDate;
    private Date revokedAt;
    private String revokedBy;
    private String tokenType;
    
    // Getters et Setters
}
```

## Points d'API

### Points d'entrée OAuth2

- **GET /api/auth/oauth2/authorize/google** : Initialisation de l'authentification Google
- **GET /api/auth/oauth2/authorize/facebook** : Initialisation de l'authentification Facebook
- **GET /oauth2/callback/google** : Callback pour Google OAuth2
- **GET /oauth2/callback/facebook** : Callback pour Facebook OAuth2

### Autres points d'API d'authentification

- **POST /api/auth/refresh-token** : Rafraîchissement du token d'accès
- **POST /api/auth/logout** : Déconnexion et révocation des tokens
- **GET /api/auth/check-auth** : Vérification de l'état d'authentification

---

## Conclusion

Le flux d'authentification OAuth2 implémenté dans ShopX offre un haut niveau de sécurité tout en maintenant une expérience utilisateur fluide. Les mesures de sécurité mises en place protègent contre les attaques courantes telles que CSRF, l'interception de token et le vol de session.

L'architecture modulaire permet d'ajouter facilement d'autres fournisseurs OAuth2 à l'avenir, et le système de gestion des tokens assure une sécurité et une flexibilité maximales.
