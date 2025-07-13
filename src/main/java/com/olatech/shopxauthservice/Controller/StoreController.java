package com.olatech.shopxauthservice.Controller;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.StoreRole;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Service.AuthorizationService;
import com.olatech.shopxauthservice.Service.StoreService;
import com.olatech.shopxauthservice.DTO.StoreDTO;
import com.olatech.shopxauthservice.Service.UserService;
import com.olatech.shopxauthservice.security.RequiresResourceAccess;
import com.olatech.shopxauthservice.security.RequiresStoreRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
public class StoreController {

    @Autowired
    private StoreService storeService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthorizationService authorizationService;

    @PostMapping("/api/stores")
    public ResponseEntity<Store> createStore(@Valid @RequestBody StoreDTO storeDTO, Authentication authentication) {
        try {
            Users currentUser = userService.getUserByUsername(authentication.getName());
            Store store = storeService.createStore(storeDTO, currentUser);
            return ResponseEntity.ok(store);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.ok().build();
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

    @PutMapping("/api/stores/{storeId}")
    @RequiresStoreRole(value = StoreRole.StoreRoleType.MANAGER)
    public ResponseEntity<Store> updateStore(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreDTO storeDTO,
            Authentication authentication) {
        Users currentUser = authorizationService.getCurrentUser(authentication);
        Store store = storeService.updateStore(storeId, storeDTO, currentUser);
        return ResponseEntity.ok(store);
    }

    @DeleteMapping("/api/stores/{storeId}")
    @RequiresStoreRole(value = StoreRole.StoreRoleType.OWNER)
    public ResponseEntity<?> deleteStore(@PathVariable Long storeId, Authentication authentication) {
        Users currentUser = authorizationService.getCurrentUser(authentication);
        storeService.deleteStore(storeId, currentUser);
        return ResponseEntity.ok().build();
    }
}

