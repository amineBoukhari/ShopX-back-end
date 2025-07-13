package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.DTO.StoreStaffDTO;
import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.StoreRole;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Repository.StoreRepository;
import com.olatech.shopxauthservice.Repository.StoreRoleRepository;
import com.olatech.shopxauthservice.exceptions.ResourceNotFoundException;
import com.olatech.shopxauthservice.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StoreStaffService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreRoleRepository storeRoleRepository;

    public List<StoreStaffDTO> getStoreStaff(Long storeId, Users user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        if (!hasAccess(store, user)) {
            throw new UnauthorizedException("Access denied");
        }

        return store.getUserRoles().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateStaffRole(Long storeId, int userId, StoreRole.StoreRoleType newRole, Users manager) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        if (!hasManagementAccess(store, manager)) {
            throw new UnauthorizedException("No permission to update roles");
        }

        StoreRole staffRole = storeRoleRepository.findByStoreAndUser_Id(store, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));

        // Prevent modifying owner's role
        if (staffRole.getRole() == StoreRole.StoreRoleType.OWNER) {
            throw new UnauthorizedException("Cannot modify owner's role");
        }

        staffRole.setRole(newRole);
        storeRoleRepository.save(staffRole);
    }

    private StoreStaffDTO convertToDTO(StoreRole storeRole) {
        StoreStaffDTO dto = new StoreStaffDTO();
        dto.setUserId(storeRole.getUser().getId());
        dto.setUsername(storeRole.getUser().getUsername());
        dto.setEmail(storeRole.getUser().getEmail());
        dto.setRole(storeRole.getRole());
        return dto;
    }

    /**
     * Checks if a user has any role in the store (basic access)
     */
    private boolean hasAccess(Store store, Users user) {
        return storeRoleRepository.findByStoreAndUser_Id(store, user.getId    ())
                .isPresent();
    }

    /**
     * Checks if a user has management privileges (OWNER or MANAGER role)
     */
    private boolean hasManagementAccess(Store store, Users user) {
        Optional<StoreRole> roleOpt = storeRoleRepository.findByStoreAndUser_Id(store, user.getId());

        if (!roleOpt.isPresent()) {
            return false;
        }

        StoreRole.StoreRoleType role = roleOpt.get().getRole();
        return role == StoreRole.StoreRoleType.OWNER ||
                role == StoreRole.StoreRoleType.MANAGER;
    }

    /**
     * Adds a new staff member to the store
     */
    @Transactional
    public void addStoreStaff(Long storeId, int userId, StoreRole.StoreRoleType role, Users manager) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        if (!hasManagementAccess(store, manager)) {
            throw new UnauthorizedException("No permission to add staff members");
        }

        // Check if user already has a role in the store
        if (storeRoleRepository.findByStoreAndUser_Id(store, userId).isPresent()) {
            throw new IllegalStateException("User already has a role in this store");
        }

        // Create new store role
        StoreRole newRole = new StoreRole();
        newRole.setStore(store);
        newRole.setUserId(userId);
        newRole.setRole(role);

        storeRoleRepository.save(newRole);
    }

    /**
     * Removes a staff member from the store
     */
    @Transactional
    public void removeStoreStaff(Long storeId, int userId, Users manager) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        StoreRole staffRole = storeRoleRepository.findByStoreAndUser_Id(store, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));

        // Prevent removing the owner
        if (staffRole.getRole() == StoreRole.StoreRoleType.OWNER) {
            throw new UnauthorizedException("Cannot remove store owner");
        }

        storeRoleRepository.delete(staffRole);
    }
}