package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.DTO.StoreDTO;
import com.olatech.shopxauthservice.DTO.WebsiteDTO;
import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.StoreRole;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Repository.StoreRepository;
import com.olatech.shopxauthservice.Repository.StoreRoleRepository;
import com.olatech.shopxauthservice.exceptions.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreService {

    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

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

    @Transactional
    public Store createStore(StoreDTO storeDTO, Users owner) {
        Store store = new Store();
        store.setName(storeDTO.getName());
        store.setDescription(storeDTO.getDescription());
        store.setLogo(storeDTO.getLogo());
        store.setOwner(owner);
        store.setSlug(generateSlug(storeDTO.getName()));

        store = storeRepository.save(store);

        StoreRole ownerRole = new StoreRole();
        ownerRole.setStore(store);
        ownerRole.setUser(owner);
        ownerRole.setRole(StoreRole.StoreRoleType.OWNER);
        storeRoleRepository.save(ownerRole);

        return store;
    }

    @Transactional
    public Store createWebsite(WebsiteDTO websiteDTO, Users owner) {
        logger.info("Creating website with DTO: {}", websiteDTO);

        if (websiteDTO == null) throw new IllegalArgumentException("WebsiteDTO cannot be null");

        String name = websiteDTO.getName();
        String subdomain = websiteDTO.getSubdomain();
        String template = websiteDTO.getTemplate();

        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Website name cannot be null or empty.");
        if (subdomain == null || subdomain.trim().isEmpty())
            throw new IllegalArgumentException("Website subdomain cannot be null or empty.");
        if (owner == null)
            throw new IllegalArgumentException("Owner cannot be null");

        String cleanName = name.trim();
        String cleanSubdomain = subdomain.trim().toLowerCase();
        String cleanTemplate = template != null ? template.trim() : "default";

        String slug = generateSlug(cleanName);

        Store store = new Store();
        store.setName(cleanName);
        store.setSubdomain(cleanSubdomain);
        store.setTemplate(cleanTemplate);
        store.setOwner(owner);
        store.setSlug(slug);
        store.setActive(true);

        store = storeRepository.save(store);

        StoreRole ownerRole = new StoreRole();
        ownerRole.setStore(store);
        ownerRole.setUser(owner);
        ownerRole.setRole(StoreRole.StoreRoleType.OWNER);
        storeRoleRepository.save(ownerRole);

        logger.info("Website created successfully with ID: {}", store.getId());
        return store;
    }

    public Store findBySlug(String slug) {
        return storeRepository.findBySlug(slug)
                .orElse(null);
    }

    private String generateSlug(String name) {
        String baseSlug = name.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (baseSlug.isEmpty()) baseSlug = "store";

        String finalSlug = baseSlug;
        int counter = 1;
        while (storeRepository.findBySlug(finalSlug).isPresent()) {
            finalSlug = baseSlug + "-" + counter;
            counter++;
        }

        return finalSlug;
    }

    public List<Store> getUserStores(Users user) {
        return storeRepository.findByOwnerOrStaffContaining(user, user);
    }

    public Store getStoreById(Long storeId, Users user) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
    }

    public Store getStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
    }

    @Transactional
    public Store updateStore(Long storeId, StoreDTO storeDTO, Users user) {
        Store store = getStoreById(storeId, user);

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
        storeRepository.delete(store);
    }

    @Transactional
    public Store assignSubdomain(Long storeId, String subdomain) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé"));

        subdomain = normalizeSubdomain(subdomain);

        if (storeRepository.existsBySubdomain(subdomain)) {
            throw new IllegalArgumentException("Ce sous-domaine est déjà utilisé");
        }

        String oldSubdomain = store.getSubdomain();
        store.setSubdomain(subdomain);
        store = storeRepository.save(store);

        if (oldSubdomain != null && !oldSubdomain.isEmpty()) {
            route53Service.deleteSubdomainRecord(oldSubdomain);
        }

        boolean dnsSuccess = route53Service.createSubdomainRecord(subdomain);
        if (!dnsSuccess) {
            logger.warn("Échec de la création de l'enregistrement DNS pour le sous-domaine: {}", subdomain);
        }

        return store;
    }

    @Transactional
    public boolean removeSubdomain(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé"));

        String currentSubdomain = store.getSubdomain();
        if (currentSubdomain == null || currentSubdomain.isEmpty()) {
            return true;
        }

        boolean dnsSuccess = route53Service.deleteSubdomainRecord(currentSubdomain);
        store.setSubdomain(null);
        storeRepository.save(store);

        return dnsSuccess;
    }

    public boolean isSubdomainAvailable(String subdomain) {
        subdomain = normalizeSubdomain(subdomain);
        return !storeRepository.existsBySubdomain(subdomain) &&
               !route53Service.subdomainExists(subdomain);
    }

    private String normalizeSubdomain(String subdomain) {
        if (subdomain == null || subdomain.isEmpty()) {
            throw new IllegalArgumentException("Le sous-domaine ne peut pas être vide");
        }

        subdomain = subdomain.toLowerCase();

        if (subdomain.endsWith("." + rootDomain)) {
            subdomain = subdomain.substring(0, subdomain.length() - rootDomain.length() - 1);
        }

        subdomain = subdomain.replaceAll("[^a-z0-9-]", "-")
                             .replaceAll("-+", "-")
                             .replaceAll("^-|-$", "");

        if (subdomain.isEmpty()) {
            throw new IllegalArgumentException("Le sous-domaine normalisé est vide");
        }

        return subdomain.length() > 63 ? subdomain.substring(0, 63) : subdomain;
    }

    public String getStoreFqdn(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé"));

        if (store.getSubdomain() == null || store.getSubdomain().isEmpty()) {
            return null;
        }

        return store.getSubdomain() + "." + rootDomain;
    }
}
