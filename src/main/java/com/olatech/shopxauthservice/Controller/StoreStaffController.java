package com.olatech.shopxauthservice.Controller;

import com.olatech.shopxauthservice.DTO.StoreStaffDTO;
import com.olatech.shopxauthservice.DTO.UpdateRoleDTO;
import com.olatech.shopxauthservice.Model.StoreRole;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Service.AuthorizationService;
import com.olatech.shopxauthservice.Service.StoreStaffService;
import com.olatech.shopxauthservice.Service.UserService;
import com.olatech.shopxauthservice.security.RequiresStoreRole;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// StoreStaffController.java
@RestController
@RequestMapping("/api/stores/{storeId}/staff")
public class StoreStaffController {

    @Autowired
    private StoreStaffService staffService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    @RequiresStoreRole(value = StoreRole.StoreRoleType.STAFF)
    public ResponseEntity<List<StoreStaffDTO>> getStoreStaff(
            @PathVariable Long storeId,
            Authentication authentication) {
        Users currentUser = authorizationService.getCurrentUser(authentication);
        List<StoreStaffDTO> staff = staffService.getStoreStaff(storeId, currentUser);
        return ResponseEntity.ok(staff);
    }

    @PutMapping("/{userId}/role")
    @RequiresStoreRole(value = StoreRole.StoreRoleType.ADMIN)
    public ResponseEntity<?> updateStaffRole(
            @PathVariable Long storeId,
            @PathVariable int userId,
            @Valid @RequestBody UpdateRoleDTO roleDTO,
            Authentication authentication) {
        Users currentUser = authorizationService.getCurrentUser(authentication);
        staffService.updateStaffRole(storeId, userId, roleDTO.getRole(), currentUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    @RequiresStoreRole(value = StoreRole.StoreRoleType.ADMIN)
    public ResponseEntity<?> removeStaffMember(
            @PathVariable Long storeId,
            @PathVariable int userId,
            Authentication authentication) {
        Users currentUser = authorizationService.getCurrentUser(authentication);
        staffService.removeStoreStaff(storeId, userId, currentUser);
        return ResponseEntity.ok().build();
    }
}
