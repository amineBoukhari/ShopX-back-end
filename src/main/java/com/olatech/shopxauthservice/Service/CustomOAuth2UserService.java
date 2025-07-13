package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    
    @Autowired
    private UserRepo userRepo;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("Processing OAuth2 authentication request for provider: {}", 
                userRequest.getClientRegistration().getRegistrationId());
        
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        
        // Log all attributes for debugging
        log.debug("OAuth2 user attributes:");
        oAuth2User.getAttributes().forEach((key, value) -> 
            log.debug("  {} = {}", key, value)
        );
        
        // Extraction des informations utilisateur
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = extractEmail(oAuth2User, provider);
        String name = extractName(oAuth2User, provider);
        String providerId = extractProviderId(oAuth2User, provider);
        
        log.info("OAuth2 login: provider={}, email={}, name={}, providerId={}", 
                provider, email, name, providerId);
        
        // Trouver ou créer l'utilisateur
        Users user = findOrCreateUser(email, name, provider, providerId);
        
        // Enrichir les attributs utilisateur et retourner un OAuth2User complet
        Map<String, Object> attributes = enrichUserAttributes(oAuth2User.getAttributes(), user);
        
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        
        log.info("Using userNameAttributeName: {}", userNameAttributeName);
        
        return new DefaultOAuth2User(
            oAuth2User.getAuthorities(),
            attributes,
            userNameAttributeName
        );
    }
    
    /**
     * Extrait l'email selon le provider
     */
    private String extractEmail(OAuth2User oAuth2User, String provider) {
        if ("google".equalsIgnoreCase(provider)) {
            return oAuth2User.getAttribute("email");
        } else if ("facebook".equalsIgnoreCase(provider)) {
            String email = oAuth2User.getAttribute("email");
            if (email == null || email.isEmpty()) {
                String id = oAuth2User.getAttribute("id");
                if (id != null) {
                    log.warn("Email not provided by Facebook, using generated email: {}@facebook.com", id);
                    return id + "@facebook.com";
                }
            }
            return email;
        }
        return oAuth2User.getAttribute("email");
    }
    
    /**
     * Extrait le nom selon le provider
     */
    private String extractName(OAuth2User oAuth2User, String provider) {
        if ("google".equalsIgnoreCase(provider)) {
            return oAuth2User.getAttribute("name");
        } else if ("facebook".equalsIgnoreCase(provider)) {
            return oAuth2User.getAttribute("name");
        }
        return oAuth2User.getAttribute("name");
    }
    
    /**
     * Extrait l'ID provider selon le fournisseur
     */
    private String extractProviderId(OAuth2User oAuth2User, String provider) {
        if ("google".equalsIgnoreCase(provider)) {
            return oAuth2User.getAttribute("sub");
        } else if ("facebook".equalsIgnoreCase(provider)) {
            return oAuth2User.getAttribute("id");
        }
        return oAuth2User.getAttribute("id");
    }
    
    /**
     * Trouve ou crée un utilisateur
     */
    private Users findOrCreateUser(String email, String name, String provider, String providerId) {
        if (email == null) {
            log.error("Cannot create user with null email");
            throw new IllegalArgumentException("Email cannot be null");
        }
        
        Optional<Users> userOptional = userRepo.findByEmail(email);
        
        if (userOptional.isPresent()) {
            // Mettre à jour l'utilisateur existant
            log.debug("User with email {} already exists, updating information", email);
            Users user = userOptional.get();
            user.setName(name);
            user.setProvider(provider);
            user.setProviderId(providerId);
            return userRepo.save(user);
        } else {
            // Créer un nouvel utilisateur
            log.info("Creating new user with email: {}", email);
            Users newUser = new Users();
            newUser.setEmail(email);
            newUser.setUsername(email);
            newUser.setName(name);
            newUser.setProvider(provider);
            newUser.setProviderId(providerId);
            newUser.setEmailVerified(true);
            
            Users savedUser = userRepo.save(newUser);
            log.info("New user created with ID: {}", savedUser.getId());
            return savedUser;
        }
    }
    
    /**
     * Enrichit les attributs utilisateur avec nos informations personnalisées
     */
    private Map<String, Object> enrichUserAttributes(Map<String, Object> attributes, Users user) {
        Map<String, Object> enrichedAttributes = new HashMap<>(attributes);
        enrichedAttributes.put("userId", user.getId());
        enrichedAttributes.put("userEmail", user.getEmail());
        enrichedAttributes.put("name", user.getName());
        return enrichedAttributes;
    }
}
