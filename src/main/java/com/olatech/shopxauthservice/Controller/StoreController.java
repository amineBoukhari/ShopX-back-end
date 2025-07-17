package com.olatech.shopxauthservice.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.StoreRole;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Service.AuthorizationService;
import com.olatech.shopxauthservice.Service.LocalFileStorageService;
import com.olatech.shopxauthservice.Service.StoreService;
import com.olatech.shopxauthservice.DTO.StoreDTO;
import com.olatech.shopxauthservice.Service.UserService;
import com.olatech.shopxauthservice.security.RequiresResourceAccess;
import com.olatech.shopxauthservice.security.RequiresStoreRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
public class StoreController {

    @Autowired
    private StoreService storeService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
   private LocalFileStorageService localFileStorageService;

    @PostMapping(value = "/api/stores", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Store> createStore(
            @RequestPart(value = "storeData", required = false) String storeJson,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @ModelAttribute StoreDTO storeRaw,
            Authentication authentication) {
        try {
            Users currentUser = userService.getUserByUsername(authentication.getName());
            StoreDTO storeDTO;

            // Handle multipart/form-data with storeData as JSON string
            if (storeJson != null) {
                ObjectMapper mapper = new ObjectMapper();
                storeDTO = mapper.readValue(storeJson, StoreDTO.class);
            }
            // Handle form data directly
            else if (storeRaw != null) {
                storeDTO = storeRaw;
            } else {
                throw new IllegalArgumentException("No store data provided");
            }

            // Handle logo upload if provided
            if (logo != null && !logo.isEmpty()) {
                try {
                    String logoUrl = localFileStorageService.uploadFile(logo);
                    storeDTO.setLogo(logoUrl);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(null);
                }
            }

            Store store = storeService.createStore(storeDTO, currentUser);
            return ResponseEntity.ok(store);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/stores")
    public ResponseEntity<List<Store>> getUserStores(Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        List<Store> stores = storeService.getUserStores(currentUser);
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/api/stores/{storeId}")
    @RequiresStoreRole(value = StoreRole.StoreRoleType.STAFF)
    public ResponseEntity<Store> getStore(@PathVariable Long storeId, Authentication authentication) {
        Users currentUser = authorizationService.getCurrentUser(authentication);
        Store store = storeService.getStoreById(storeId, currentUser);
        return ResponseEntity.ok(store);
    }

    @PutMapping(value = "/api/stores/{storeId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE })
    @RequiresStoreRole(value = StoreRole.StoreRoleType.MANAGER)
    public ResponseEntity<Store> updateStore(
            @PathVariable Long storeId,
            @RequestPart(value = "storeData", required = false) String storeJson,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @ModelAttribute StoreDTO storeRaw,
            Authentication authentication) {
        try {
            Users currentUser = authorizationService.getCurrentUser(authentication);
            StoreDTO storeDTO;

            // Handle multipart/form-data with storeData as JSON string
            if (storeJson != null) {
                ObjectMapper mapper = new ObjectMapper();
                storeDTO = mapper.readValue(storeJson, StoreDTO.class);
            }
            // Handle form data directly
            else if (storeRaw != null) {
                storeDTO = storeRaw;
            } else {
                throw new IllegalArgumentException("No store data provided");
            }

            // Handle logo upload if provided
            if (logo != null && !logo.isEmpty()) {
                try {
                    String logoUrl = localFileStorageService.uploadFile(logo);
                    storeDTO.setLogo(logoUrl);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(null);
                }
            }

            Store store = storeService.updateStore(storeId, storeDTO, currentUser);
            return ResponseEntity.ok(store);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/api/stores/{storeId}")
    @RequiresStoreRole(value = StoreRole.StoreRoleType.OWNER)
    public ResponseEntity<?> deleteStore(@PathVariable Long storeId, Authentication authentication) {
        Users currentUser = authorizationService.getCurrentUser(authentication);
        storeService.deleteStore(storeId, currentUser);
        return ResponseEntity.ok().build();
    }
}