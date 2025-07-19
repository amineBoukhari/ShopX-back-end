package com.olatech.shopxauthservice.Controller;

import com.olatech.shopxauthservice.DTO.WebsiteDTO;
import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Service.OpenAIService;
import com.olatech.shopxauthservice.Service.StoreService;
import com.olatech.shopxauthservice.Service.ThemeFileService;
import com.olatech.shopxauthservice.Service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/websites")
public class WebsiteController {

    private static final Logger logger = LoggerFactory.getLogger(WebsiteController.class);

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserService userService;

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private ThemeFileService themeFileService;

    @GetMapping("/{slug}")
    public ResponseEntity<?> getStoreBySlug(@PathVariable String slug) {
        Store store = storeService.findBySlug(slug);
        if (store == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error", "Store not found")
            );
        }
        return ResponseEntity.ok(store);
    }

    @PostMapping
    public ResponseEntity<?> createWebsite(@Valid @RequestBody WebsiteDTO websiteDTO,
                                           Authentication authentication) {
        try {
            logger.info("Creating website: {}", websiteDTO.getName());

            Users currentUser = userService.getUserByUsername(authentication.getName());

            // ✅ Basic validation
            if (!websiteDTO.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", "Invalid website data")
                );
            }

            // ✅ Check subdomain availability
            if (!storeService.isSubdomainAvailable(websiteDTO.getSubdomain())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("error", "Subdomain already exists")
                );
            }

            String subdomain = websiteDTO.getSubdomain();
            String websiteName = websiteDTO.getName();
            String websiteUrl = "https://" + subdomain + ".shopx.com";

            // ✅ Handle AI theme generation
            if ("ai".equals(websiteDTO.getCreationMethod())) {
                logger.info("🤖 Generating AI theme for: {}", websiteDTO.getThemePrompt());
                
                try {
                    // Generate AI theme
                    Map<String, Object> aiTheme = openAIService.generateWebsiteTheme(websiteDTO.getThemePrompt());
                    logger.info("✅ AI theme generated successfully");
                    
                    // Generate theme files - Pass website name instead of subdomain
                    themeFileService.generateThemeFiles(websiteName, aiTheme);
                    logger.info("📁 Theme files created for website: {}", websiteName);
                    
                } catch (Exception aiError) {
                    logger.error("❌ AI theme generation failed: {}", aiError.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        Map.of("error", "Failed to generate AI theme: " + aiError.getMessage())
                    );
                }
            }

            // ✅ Create basic website record (minimal data)
            Store store = storeService.createSimpleWebsite(websiteDTO, currentUser);
            logger.info("✅ Website created: {}", store.getSlug());

            // ✅ Return success with redirect URL
            return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                    "success", true,
                    "message", "Website created successfully!",
                    "websiteUrl", websiteUrl,
                    "subdomain", subdomain,
                    "redirectTo", "/sites/" + subdomain,  // For frontend redirect
                    "store", Map.of(
                        "id", store.getId(),
                        "name", store.getName(),
                        "slug", store.getSlug(),
                        "subdomain", store.getSubdomain()
                    )
                )
            );

        } catch (Exception e) {
            logger.error("❌ Error creating website: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Internal server error: " + e.getMessage())
            );
        }
    }

    // ✅ Simple endpoint to check if website has theme files
    @GetMapping("/{subdomain}/theme-status")
    public ResponseEntity<?> getThemeStatus(@PathVariable String subdomain) {
        try {
            boolean hasThemeFiles = themeFileService.themeFilesExist(subdomain);
            
            return ResponseEntity.ok(Map.of(
                "subdomain", subdomain,
                "hasThemeFiles", hasThemeFiles,
                "themeUrl", "/themes/" + subdomain + "/theme.css",
                "configUrl", "/themes/" + subdomain + "/config.js"
            ));
            
        } catch (Exception e) {
            logger.error("Error checking theme status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Failed to check theme status")
            );
        }
    }

    // ✅ Test endpoint for AI theme generation
    @PostMapping("/test-ai-theme")
    public ResponseEntity<?> testAITheme(@RequestBody Map<String, String> request) {
        try {
            String prompt = request.get("prompt");
            if (prompt == null || prompt.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Prompt is required")
                );
            }

            Map<String, Object> theme = openAIService.generateWebsiteTheme(prompt);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "prompt", prompt,
                "theme", theme
            ));

        } catch (Exception e) {
            logger.error("Error testing AI theme: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", e.getMessage())
            );
        }
    }
}