package com.olatech.shopxauthservice.security.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Classe responsable de la validation des URLs de redirection OAuth2
 * pour prévenir les attaques de redirection.
 */
@Component
public class OAuth2RedirectValidator {

    @Value("${oauth2.authorized-redirect-uris:https://app-shopx.olatechsn.com/app,http://localhost:3000/app}")
    private String authorizedRedirectUris;

    /**
     * Vérifie si une URL de redirection est autorisée.
     * 
     * @param redirectUri L'URL de redirection à valider
     * @return true si l'URL est autorisée, false sinon
     */
    public boolean isAuthorized(String redirectUri) {
        if (redirectUri == null || redirectUri.isEmpty()) {
            return false;
        }

        List<String> allowedUris = getAllowedRedirectUris();

        try {
            URI uri = new URI(redirectUri);
            String redirectUriWithoutQuery = uri.getScheme() + "://" + uri.getHost() + 
                    (uri.getPort() != -1 ? ":" + uri.getPort() : "") + uri.getPath();

            return allowedUris.stream()
                    .anyMatch(allowedUri -> redirectUriWithoutQuery.startsWith(allowedUri));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Valide une URL de redirection et la retourne si elle est autorisée.
     * 
     * @param redirectUri L'URL de redirection à valider
     * @return L'URL validée
     * @throws IllegalArgumentException si l'URL n'est pas autorisée
     */
    public String validateAndGetRedirectUri(String redirectUri) {
        if (!isAuthorized(redirectUri)) {
            throw new IllegalArgumentException("URL de redirection non autorisée: " + redirectUri);
        }
        return redirectUri;
    }

    /**
     * Retourne l'URL de redirection par défaut.
     * 
     * @return L'URL de redirection par défaut
     */
    public String getDefaultRedirectUri() {
        List<String> allowedUris = getAllowedRedirectUris();
        return allowedUris.isEmpty() ? null : allowedUris.get(0);
    }

    /**
     * Récupère la liste des URLs de redirection autorisées depuis la configuration.
     */
    private List<String> getAllowedRedirectUris() {
        return Arrays.asList(authorizedRedirectUris.trim().split("\\s*,\\s*"));
    }
}
