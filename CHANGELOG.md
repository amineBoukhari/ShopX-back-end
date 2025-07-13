# Changelog - Sécurisation des flux OAuth2

## [2.0.0] - 2025-03-12

### Ajouté
- Implémentation complète du flux OAuth2 sécurisé pour Google et Facebook
- Gestion du paramètre "state" pour prévenir les attaques CSRF
- Validation des URLs de redirection contre une liste blanche
- Protection PKCE pour Google OAuth2
- Stockage sécurisé des tokens dans des cookies HttpOnly, SameSite et Secure
- Table des tokens révoqués pour invalidation des tokens lors de la déconnexion
- Enrichissement des attributs utilisateur lors de l'authentification OAuth2 (image de profil, vérification d'email)
- Support des migrations de base de données via Flyway
- Documentation détaillée du flux OAuth2 et instructions de configuration
- Index de performance pour optimiser les recherches d'utilisateurs et de sessions

### Modifié
- Amélioration du modèle Users pour prendre en charge les attributs OAuth2
- Refonte complète du flux d'authentification frontend/backend
- Mise à jour du middleware d'authentification pour une gestion sécurisée
- Extraction standardisée des attributs utilisateur selon les fournisseurs OAuth2
- Gestion améliorée des sessions utilisateur

### Corrigé
- Exposition des tokens dans les URLs remplacée par des cookies sécurisés
- Absence de vérification state lors des retours OAuth2
- Gestion insuffisante des cas particuliers (email manquant, etc.)
- Nettoyage manuel des tokens révoqués remplacé par un job planifié

## [1.0.0] - Initial Release

- Version initiale de l'authentification
