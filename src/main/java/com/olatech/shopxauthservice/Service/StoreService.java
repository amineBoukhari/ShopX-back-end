// StoreService.java
package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Model.StoreRole;
import com.olatech.shopxauthservice.Repository.StoreRepository;
import com.olatech.shopxauthservice.Repository.StoreRoleRepository;
import com.olatech.shopxauthservice.DTO.StoreDTO;
import com.olatech.shopxauthservice.exceptions.ResourceNotFoundException;
import com.olatech.shopxauthservice.exceptions.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreRoleRepository storeRoleRepository;
    
    @Autowired
    private AuthorizationService authorizationService;
    
    @Autowired
    private Route53Service route53Service;
    
    @Value("${aws.route53.root.domain}")
    private String rootDomain;
    
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    @Transactional
    public Store createStore(StoreDTO storeDTO, Users owner) {
        Store store = new Store();
        store.setName(storeDTO.getName());
        store.setDescription(storeDTO.getDescription());
        store.setLogo(storeDTO.getLogo());
        store.setOwner(owner);
        store.setSlug(generateSlug(storeDTO.getName()));
        store = storeRepository.save(store);

        // Créer le rôle OWNER pour le créateur
        StoreRole ownerRole = new StoreRole();
        ownerRole.setStore(store);
        ownerRole.setUser(owner);
        ownerRole.setRole(StoreRole.StoreRoleType.OWNER);
        storeRoleRepository.save(ownerRole);

        return store;
    }
    
    /**
     * Génère un slug unique pour un magasin à partir de son nom
     */
    private String generateSlug(String name) {
        String baseSlug = name.toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        
        String slug = baseSlug;
        int counter = 1;
        
        // Vérifier si le slug existe déjà et générer un nouveau si nécessaire
        while (storeRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }

    public List<Store> getUserStores(Users user) {
        return storeRepository.findByOwnerOrStaffContaining(user, user);
    }

    public Store getStoreById(Long storeId, Users user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        // Authorization is now handled by the AuthorizationService via annotations
        // We no longer need to check access here as it's done at the controller level

        return store;
    }

    public Store getStoreById(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        // Authorization is now handled by the AuthorizationService via annotations
        // We no longer need to check access here as it's done at the controller level

        return store;
    }



    @Transactional
    public Store updateStore(Long storeId, StoreDTO storeDTO, Users user) {
        Store store = getStoreById(storeId, user);

        // Authorization is now handled by the @RequiresStoreRole annotation
        // No need to check permissions here as it's done at the controller level

        store.setName(storeDTO.getName());
        store.setDescription(storeDTO.getDescription());
        store.setLogo(storeDTO.getLogo());
        store.setFacebookBusinessId(storeDTO.getFacebookBusinessId());
        store.setFacebookCatalogId(storeDTO.getFacebookCatalogId());
        store.setFacebookToken(storeDTO.getFacebookToken());

        return storeRepository.save(store);
    }

    @Transactional
    public void deleteStore(Long storeId, Users user) {
        Store store = getStoreById(storeId, user);

        // Authorization is now handled by the @RequiresStoreRole annotation
        // No need to check permissions here as it's done at the controller level

        storeRepository.delete(store);
    }

    private boolean hasAccess(Store store, Users user) {
        return isOwner(store, user) || isStaff(store, user);
    }

    private boolean isOwner(Store store, Users user) {
        return store.getOwner().getId() == user.getId();
    }

    private boolean isStaff(Store store, Users user) {
        return store.getStaff().contains(user);
    }
    
    /**
     * Attribue un sous-domaine à un magasin
     * @param storeId ID du magasin
     * @param subdomain Nom du sous-domaine (sans le domaine racine)
     * @return Le magasin mis à jour
     */
    @Transactional
    public Store assignSubdomain(Long storeId, String subdomain) {
        // Vérifier que le magasin existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé"));
        
        // Normaliser le sous-domaine
        subdomain = normalizeSubdomain(subdomain);
        
        // Vérifier que le sous-domaine est disponible dans la base de données
        if (storeRepository.existsBySubdomain(subdomain)) {
            throw new IllegalArgumentException("Ce sous-domaine est déjà utilisé");
        }
        
        // Vérifier si le magasin a déjà un sous-domaine
        String oldSubdomain = store.getSubdomain();
        
        // Assigner le nouveau sous-domaine
        store.setSubdomain(subdomain);
        store = storeRepository.save(store);
        
        // Supprimer l'ancien enregistrement DNS si nécessaire
        if (oldSubdomain != null && !oldSubdomain.isEmpty()) {
            logger.info("Suppression de l'ancien sous-domaine: {}", oldSubdomain);
            route53Service.deleteSubdomainRecord(oldSubdomain);
        }
        
        // Créer le nouvel enregistrement DNS
        boolean dnsSuccess = route53Service.createSubdomainRecord(subdomain);
        if (!dnsSuccess) {
            logger.warn("Échec de la création de l'enregistrement DNS pour le sous-domaine: {}", subdomain);
        }
        
        return store;
    }
    
    /**
     * Supprime le sous-domaine associé à un magasin
     * @param storeId ID du magasin
     * @return true si l'opération a réussi
     */
    @Transactional
    public boolean removeSubdomain(Long storeId) {
        // Vérifier que le magasin existe
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé"));
        
        // Vérifier si le magasin a un sous-domaine
        String currentSubdomain = store.getSubdomain();
        if (currentSubdomain == null || currentSubdomain.isEmpty()) {
            return true; // Rien à faire
        }
        
        // Supprimer l'enregistrement DNS
        boolean dnsSuccess = route53Service.deleteSubdomainRecord(currentSubdomain);
        
        // Retirer le sous-domaine du magasin
        store.setSubdomain(null);
        storeRepository.save(store);
        
        return dnsSuccess;
    }
    
    /**
     * Vérifie si un sous-domaine est disponible
     * @param subdomain Nom du sous-domaine à vérifier
     * @return true si le sous-domaine est disponible
     */
    public boolean isSubdomainAvailable(String subdomain) {
        // Normaliser le sous-domaine
        subdomain = normalizeSubdomain(subdomain);
        
        // Vérifier dans la base de données
        if (storeRepository.existsBySubdomain(subdomain)) {
            return false;
        }
        
        // Vérifier dans Route53 (pour les sous-domaines qui pourraient exister dans Route53 mais pas dans notre BD)
        return !route53Service.subdomainExists(subdomain);
    }
    
    /**
     * Normalise un sous-domaine pour s'assurer qu'il est valide et dans le format attendu
     * @param subdomain Sous-domaine à normaliser
     * @return Sous-domaine normalisé
     */
    private String normalizeSubdomain(String subdomain) {
        if (subdomain == null || subdomain.isEmpty()) {
            throw new IllegalArgumentException("Le sous-domaine ne peut pas être vide");
        }
        
        // Convertir en minuscules
        subdomain = subdomain.toLowerCase();
        
        // Supprimer le domaine racine s'il est inclus
        if (subdomain.endsWith("." + rootDomain)) {
            subdomain = subdomain.substring(0, subdomain.length() - rootDomain.length() - 1);
        }
        
        // Remplacer les caractères invalides
        subdomain = subdomain.replaceAll("[^a-z0-9-]", "-");
        
        // Supprimer les tirets multiples
        subdomain = subdomain.replaceAll("-+", "-");
        
        // Supprimer les tirets au début et à la fin
        subdomain = subdomain.replaceAll("^-|-$", "");
        
        // Vérifier la longueur minimale
        if (subdomain.isEmpty()) {
            throw new IllegalArgumentException("Le sous-domaine normalisé est vide");
        }
        
        // Vérifier la longueur maximale (pour éviter les problèmes avec les limites DNS)
        if (subdomain.length() > 63) {
            subdomain = subdomain.substring(0, 63);
        }
        
        return subdomain;
    }
    
    /**
     * Obtient le FQDN (Fully Qualified Domain Name) pour un magasin
     * @param storeId ID du magasin
     * @return Le FQDN du magasin ou null si aucun sous-domaine n'est défini
     */
    public String getStoreFqdn(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé"));
        
        if (store.getSubdomain() == null || store.getSubdomain().isEmpty()) {
            return null;
        }
        
        return store.getSubdomain() + "." + rootDomain;
    }
}

