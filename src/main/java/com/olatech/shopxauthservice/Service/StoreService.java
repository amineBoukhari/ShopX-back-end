package com.olatech.shopxauthservice.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private OpenAIService openAIService;

    @Value("${aws.route53.root.domain}")
    private String rootDomain;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    /**
     * Updated createWebsite method with AI theme generation support
     */
    @Transactional
    public Store createWebsite(WebsiteDTO websiteDTO, Users owner) {
        logger.info("Creating website with DTO: {}", websiteDTO);

        logger.info("DEBUG: Creation method received: '{}'", websiteDTO.getCreationMethod());
        logger.info("DEBUG: Theme prompt received: '{}'", websiteDTO.getThemePrompt());
        logger.info("DEBUG: Template received: '{}'", websiteDTO.getTemplate());

        if (websiteDTO == null) throw new IllegalArgumentException("WebsiteDTO cannot be null");

        String name = websiteDTO.getName();
        String subdomain = websiteDTO.getSubdomain();
        String creationMethod = websiteDTO.getCreationMethod();
        String template = websiteDTO.getTemplate();
        String themePrompt = websiteDTO.getThemePrompt();

        // Basic validation
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Website name cannot be null or empty.");
        if (subdomain == null || subdomain.trim().isEmpty())
            throw new IllegalArgumentException("Website subdomain cannot be null or empty.");
        if (owner == null)
            throw new IllegalArgumentException("Owner cannot be null");

        // Handle backward compatibility - default to template method if not specified
        if (creationMethod == null || creationMethod.trim().isEmpty()) {
            creationMethod = "template";
        }

        // Method-specific validation
        if ("template".equals(creationMethod) && (template == null || template.trim().isEmpty())) {
            // For backward compatibility, use default template if not specified
            template = "default";
        }
        if ("ai".equals(creationMethod) && (themePrompt == null || themePrompt.trim().length() < 10)) {
            throw new IllegalArgumentException("Theme prompt is required and must be at least 10 characters when using AI method");
        }

        String cleanName = name.trim();
        String cleanSubdomain = subdomain.trim().toLowerCase();
        String cleanCreationMethod = creationMethod.trim();
        String cleanTemplate = template != null ? template.trim() : "default";
        String cleanThemePrompt = themePrompt != null ? themePrompt.trim() : null;

        String slug = generateSlug(cleanName);

        // Handle AI theme generation
        Map<String, Object> aiGeneratedTheme = null;
        if ("ai".equals(cleanCreationMethod)) {
            logger.info("DEBUG: AI METHOD DETECTED - Starting AI theme generation");
            logger.info("DEBUG: Theme prompt for AI: '{}'", websiteDTO.getThemePrompt());
            
      try {
    logger.info("DEBUG: Calling OpenAI with prompt: '{}'", cleanThemePrompt);
    
        aiGeneratedTheme = openAIService.generateWebsiteTheme(cleanThemePrompt);
        
        logger.info("DEBUG: OpenAI call completed successfully");
        logger.info("DEBUG: AI theme response: {}", aiGeneratedTheme);
        
        // Debug specific parts of the response
        if (aiGeneratedTheme != null) {
            logger.info("DEBUG: Theme template ID: {}", aiGeneratedTheme.get("templateId"));
            logger.info("DEBUG: Theme title: {}", aiGeneratedTheme.get("title"));
            logger.info("DEBUG: Theme colors: {}", aiGeneratedTheme.get("colors"));
            logger.info("DEBUG: Theme style: {}", aiGeneratedTheme.get("style"));
            logger.info("DEBUG: Theme content: {}", aiGeneratedTheme.get("content"));
        } else {
            logger.warn("DEBUG: AI theme response is NULL!");
        }
        
    } catch (Exception aiError) {
        logger.error("DEBUG: AI theme generation failed with error: {}", aiError.getMessage(), aiError);
        logger.error("DEBUG: Error class: {}", aiError.getClass().getSimpleName());
        throw new RuntimeException("Failed to generate AI theme: " + aiError.getMessage());
    }
        }

        // Create the store/website
        Store store = new Store();
        store.setName(cleanName);
        store.setSubdomain(cleanSubdomain);
        store.setOwner(owner);
        store.setSlug(slug);
        store.setActive(true);
        store.setCreatedAt(new Date());
        
        // Set creation method
        store.setCreationMethod(cleanCreationMethod);
        
        // Handle template vs AI
        if ("template".equals(cleanCreationMethod)) {
            store.setTemplate(cleanTemplate);
            store.setTemplateId(cleanTemplate);
        } else if ("ai".equals(cleanCreationMethod)) {
            store.setTemplate("ai-generated");
            store.setTemplateId("ai-generated");
            store.setOriginalPrompt(cleanThemePrompt);
            
            // Store AI theme data as JSON string
            if (aiGeneratedTheme != null) {
                try {
                    store.setAiGeneratedTheme(objectMapper.writeValueAsString(aiGeneratedTheme));
                } catch (Exception e) {
                    logger.error("Error serializing AI theme data: {}", e.getMessage());
                    store.setAiGeneratedTheme(null);
                }
            }
        }

        store = storeRepository.save(store);

        // Create owner role
        StoreRole ownerRole = new StoreRole();
        ownerRole.setStore(store);
        ownerRole.setUser(owner);
        ownerRole.setRole(StoreRole.StoreRoleType.OWNER);
        storeRoleRepository.save(ownerRole);

        logger.info("Website created successfully with ID: {} using method: {}", store.getId(), cleanCreationMethod);
        return store;
    }

    /**
     * Backward compatibility method - delegates to new method with AI theme support
     */
    @Transactional
    public Store createWebsite(WebsiteDTO websiteDTO, Users owner, Map<String, Object> aiGeneratedTheme) {
        // This method signature is for backward compatibility with the controller
        // The actual AI generation happens in the main createWebsite method above
        return createWebsite(websiteDTO, owner);
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

    /**
     * Helper method to get AI theme data as Map for a store
     */
    public Map<String, Object> getAiThemeData(Long storeId) {
        Store store = getStoreById(storeId);
        
        if (store.getAiGeneratedTheme() == null || store.getAiGeneratedTheme().trim().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(store.getAiGeneratedTheme(), Map.class);
        } catch (Exception e) {
            logger.error("Error deserializing AI theme data for store {}: {}", storeId, e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to update AI theme data for a store
     */
    @Transactional
    public Store updateAiThemeData(Long storeId, Map<String, Object> themeData) {
        Store store = getStoreById(storeId);
        
        try {
            store.setAiGeneratedTheme(objectMapper.writeValueAsString(themeData));
            return storeRepository.save(store);
        } catch (Exception e) {
            logger.error("Error updating AI theme data for store {}: {}", storeId, e.getMessage());
            throw new RuntimeException("Failed to update AI theme data: " + e.getMessage());
        }
    }
    @Transactional
public Store createSimpleWebsite(WebsiteDTO websiteDTO, Users owner) {
    logger.info("Creating simple website: {}", websiteDTO.getName());

    String cleanName = websiteDTO.getName().trim();
    String cleanSubdomain = websiteDTO.getSubdomain().trim().toLowerCase();
    String slug = generateSlug(cleanName);

    // Create simple store record
    Store store = new Store();
    store.setName(cleanName);
    store.setSubdomain(cleanSubdomain);
    store.setSlug(slug);
    store.setOwner(owner);
    store.setActive(true);
    store.setCreatedAt(new Date());
    
    // Set creation method for reference
    store.setCreationMethod(websiteDTO.getCreationMethod());
    
    // Save to database
    store = storeRepository.save(store);

    // Create owner role
    StoreRole ownerRole = new StoreRole();
    ownerRole.setStore(store);
    ownerRole.setUser(owner);
    ownerRole.setRole(StoreRole.StoreRoleType.OWNER);
    storeRoleRepository.save(ownerRole);

    logger.info("Simple website created with ID: {}", store.getId());
    return store;
}
}