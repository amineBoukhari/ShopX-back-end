package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.StoreRole;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Repository.StoreRepository;
import com.olatech.shopxauthservice.Repository.StoreRoleRepository;
import com.olatech.shopxauthservice.exceptions.ResourceNotFoundException;
import com.olatech.shopxauthservice.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Centralized service for handling all authorization decisions in the application.
 * This service encapsulates the logic for determining if a user has the required
 * permissions to perform specific actions on resources.
 */
@Service
public class AuthorizationService {
    
    @Autowired
    private StoreRoleRepository storeRoleRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * Validates if a user has at least the specified role for a store.
     * Throws UnauthorizedException if the user doesn't have sufficient permissions.
     *
     * @param user The user to check
     * @param storeId The store ID
     * @param requiredRole The minimum required role
     * @throws UnauthorizedException if the user doesn't have sufficient permissions
     * @throws ResourceNotFoundException if the store doesn't exist
     */
    public void validateStoreRole(Users user, Long storeId, StoreRole.StoreRoleType requiredRole) {
        if (!hasStoreRole(user, storeId, requiredRole)) {
            throw new UnauthorizedException("Insufficient permissions for store with ID: " + storeId);
        }
    }
    
    /**
     * Checks if a user has at least the specified role for a store.
     *
     * @param user The user to check
     * @param storeId The store ID
     * @param minimumRole The minimum required role
     * @return true if the user has the required role or higher, false otherwise
     */
    public boolean hasStoreRole(Users user, Long storeId, StoreRole.StoreRoleType minimumRole) {
        // Ensure the store exists
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));
        
        // Check if user is the owner (special case)
        if (store.getOwner().getId() == user.getId()) {
            return true; // Owner has all permissions
        }
        
        // Find the user's role for this store
        Optional<StoreRole> roleOpt = storeRoleRepository.findByUserAndStore(user, store);
        if (roleOpt.isEmpty()) {
            return false; // User has no role for this store
        }
        
        StoreRole userRole = roleOpt.get();
        StoreRole.StoreRoleType role = userRole.getRole();
        
        // Role hierarchy check: OWNER > ADMIN > MANAGER > STAFF
        switch (minimumRole) {
            case STAFF:
                return true; // Any role grants access to STAFF-level operations
            case MANAGER:
                return role == StoreRole.StoreRoleType.MANAGER || 
                       role == StoreRole.StoreRoleType.ADMIN || 
                       role == StoreRole.StoreRoleType.OWNER;
            case ADMIN:
                return role == StoreRole.StoreRoleType.ADMIN || 
                       role == StoreRole.StoreRoleType.OWNER;
            case OWNER:
                return role == StoreRole.StoreRoleType.OWNER;
            default:
                return false;
        }
    }
    
    /**
     * Validates if a user has access to a specific resource.
     * Throws UnauthorizedException if the user doesn't have sufficient permissions.
     *
     * @param user The user to check
     * @param resourceType The type of resource being accessed
     * @param resourceId The ID of the resource
     * @param action The action being performed
     * @throws UnauthorizedException if the user doesn't have sufficient permissions
     */
    public void validateResourceAccess(Users user, String resourceType, Long resourceId, String action) {
        if (!hasResourceAccess(user, resourceType, resourceId, action)) {
            throw new UnauthorizedException("Access denied for " + resourceType + " " + resourceId);
        }
    }
    
    /**
     * Checks if a user has access to a specific resource.
     *
     * @param user The user to check
     * @param resourceType The type of resource being accessed
     * @param resourceId The ID of the resource
     * @param action The action being performed
     * @return true if the user has the required permissions, false otherwise
     */
    public boolean hasResourceAccess(Users user, String resourceType, Long resourceId, String action) {
        // Map resource types to specific authorization checks
        switch (resourceType) {
            case "store":
                return hasStoreAccess(user, resourceId, action);
            case "invitation":
                return hasInvitationAccess(user, resourceId, action);
            // Add other resource types as needed
            default:
                return false;
        }
    }
    
    /**
     * Checks if a user has access to perform an action on a store.
     *
     * @param user The user to check
     * @param storeId The store ID
     * @param action The action being performed
     * @return true if the user has the required permissions, false otherwise
     */
    private boolean hasStoreAccess(Users user, Long storeId, String action) {
        switch (action) {
            case "view":
                return hasStoreRole(user, storeId, StoreRole.StoreRoleType.STAFF);
            case "edit":
                return hasStoreRole(user, storeId, StoreRole.StoreRoleType.MANAGER);
            case "delete":
                return hasStoreRole(user, storeId, StoreRole.StoreRoleType.OWNER);
            default:
                return false;
        }
    }
    
    /**
     * Checks if a user has access to an invitation.
     *
     * @param user The user to check
     * @param invitationId The invitation ID
     * @param action The action being performed
     * @return true if the user has the required permissions, false otherwise
     */
    private boolean hasInvitationAccess(Users user, Long invitationId, String action) {
        // Implementation would depend on how invitations are structured
        // For now, returning a placeholder implementation
        return true; // This should be replaced with actual logic
    }
    
    /**
     * Utility method to get the current user from Authentication
     *
     * @param authentication The current authentication context
     * @return The current user
     * @throws UnauthorizedException if the user is not found
     */
    public Users getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return userService.getUserByUsername(authentication.getName());
    }
    
    /**
     * Returns a list of permissions for a user based on their store roles.
     * Used for generating JWT tokens.
     *
     * @param user The user to get permissions for
     * @return A list of permission strings
     */
    public List<String> getUserPermissions(Users user) {
        List<String> permissions = new ArrayList<>();
        List<StoreRole> roles = storeRoleRepository.findByUser(user);
        
        for (StoreRole role : roles) {
            Long storeId = role.getStore().getId();
            
            switch (role.getRole()) {
                case OWNER:
                    permissions.add("store:" + storeId + ":full_access");
                    break;
                case ADMIN:
                    permissions.add("store:" + storeId + ":manage");
                    break;
                case MANAGER:
                    permissions.add("store:" + storeId + ":edit");
                    break;
                case STAFF:
                    permissions.add("store:" + storeId + ":view");
                    break;
            }
        }
        
        return permissions;
    }
}
