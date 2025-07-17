package com.olatech.shopxauthservice.Controller;

import com.olatech.shopxauthservice.DTO.WebsiteDTO;
import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Service.StoreService;
import com.olatech.shopxauthservice.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/websites")
public class WebsiteController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserService userService;

    
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
            Users currentUser = userService.getUserByUsername(authentication.getName());

            // ✅ Validate subdomain
            if (!storeService.isSubdomainAvailable(websiteDTO.getSubdomain())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("error", "Subdomain already exists")
                );
            }

            Store store = storeService.createWebsite(websiteDTO, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                    "success", true,
                    "message", "Website created successfully",
                    "store", store,
                    "websiteUrl", "https://" + store.getSubdomain() + ".shopx.com"
                )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", e.getMessage())
            );
        }
    }
}
