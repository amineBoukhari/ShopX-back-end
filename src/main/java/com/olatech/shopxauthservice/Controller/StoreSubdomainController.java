package com.olatech.shopxauthservice.Controller;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.StoreRole;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Service.AuthorizationService;
import com.olatech.shopxauthservice.Service.StoreService;
import com.olatech.shopxauthservice.exceptions.ResourceNotFoundException;
import com.olatech.shopxauthservice.security.RequiresStoreRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour gérer les sous-domaines des boutiques
 */
@RestController
@RequestMapping("/api/stores")
public class StoreSubdomainController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Vérifie la disponibilité d'un sous-domaine
     *
     * @param subdomain Le sous-domaine à vérifier
     * @return Un objet contenant un statut de disponibilité
     */
    @GetMapping("/subdomain/check")
    public ResponseEntity<Map<String, Object>> checkSubdomainAvailability(
            @RequestParam String subdomain) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isAvailable = storeService.isSubdomainAvailable(subdomain);
            response.put("available", isAvailable);
            
            if (!isAvailable) {
                response.put("message", "Ce sous-domaine est déjà utilisé");
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("available", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Affecte un sous-domaine à une boutique
     *
     * @param storeId ID de la boutique
     * @param requestBody Contient le sous-domaine à affecter
     * @param authentication Informations d'authentification
     * @return La boutique mise à jour
     */
    @PostMapping("/{storeId}/subdomain")
    @RequiresStoreRole(value = StoreRole.StoreRoleType.MANAGER)
    public ResponseEntity<Map<String, Object>> assignSubdomain(
            @PathVariable Long storeId,
            @RequestBody Map<String, String> requestBody,
            Authentication authentication) {
        
        String subdomain = requestBody.get("subdomain");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Store updatedStore = storeService.assignSubdomain(storeId, subdomain);
            
            response.put("success", true);
            response.put("store", updatedStore);
            response.put("subdomain", updatedStore.getSubdomain());
            response.put("fqdn", storeService.getStoreFqdn(storeId));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprime le sous-domaine d'une boutique
     *
     * @param storeId ID de la boutique
     * @param authentication Informations d'authentification
     * @return Statut de l'opération
     */
    @DeleteMapping("/{storeId}/subdomain")
    @RequiresStoreRole(value = StoreRole.StoreRoleType.MANAGER)
    public ResponseEntity<Map<String, Object>> removeSubdomain(
            @PathVariable Long storeId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();

        try {
            boolean success = storeService.removeSubdomain(storeId);
            
            response.put("success", success);
            
            if (!success) {
                response.put("message", "Erreur lors de la suppression du sous-domaine dans DNS");
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Récupère les informations de sous-domaine d'une boutique
     *
     * @param storeId ID de la boutique
     * @param authentication Informations d'authentification
     * @return Information sur le sous-domaine
     */
    @GetMapping("/{storeId}/subdomain")
    @RequiresStoreRole(value = StoreRole.StoreRoleType.STAFF)
    public ResponseEntity<Map<String, Object>> getSubdomainInfo(
            @PathVariable Long storeId,
            Authentication authentication) {
        
        try {
            Store store = storeService.getStoreById(storeId, authorizationService.getCurrentUser(authentication));
            
            Map<String, Object> response = new HashMap<>();
            response.put("storeId", storeId);
            response.put("subdomain", store.getSubdomain());
            
            if (store.getSubdomain() != null && !store.getSubdomain().isEmpty()) {
                response.put("fqdn", storeService.getStoreFqdn(storeId));
                response.put("hasSubdomain", true);
            } else {
                response.put("hasSubdomain", false);
            }
            
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
