package com.olatech.shopxauthservice.Controller;

import com.olatech.shopxauthservice.DTO.StoreVisitorDTO;
import com.olatech.shopxauthservice.Service.StoreVisitorService;
import com.olatech.shopxauthservice.security.RequiresStoreRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing store visitors
 */
@RestController
@RequestMapping("/api/stores/{storeId}/visitors")
@RequiredArgsConstructor
public class StoreVisitorController {

    private final StoreVisitorService storeVisitorService;

    /**
     * Get all visitors for a store
     *
     * @param storeId The store ID
     * @param authentication The authenticated user
     * @return List of store visitors
     */
    @GetMapping
    @RequiresStoreRole(value = com.olatech.shopxauthservice.Model.StoreRole.StoreRoleType.STAFF)
    public ResponseEntity<List<StoreVisitorDTO>> getAllVisitors(
            @PathVariable Long storeId,
            Authentication authentication) {
        
        List<StoreVisitorDTO> visitors = storeVisitorService.getAllVisitors(storeId);
        return ResponseEntity.ok(visitors);
    }

    /**
     * Get a specific visitor by ID
     *
     * @param storeId The store ID
     * @param visitorId The visitor ID
     * @param authentication The authenticated user
     * @return The store visitor
     */
    @GetMapping("/{visitorId}")
    @RequiresStoreRole(value = com.olatech.shopxauthservice.Model.StoreRole.StoreRoleType.STAFF)
    public ResponseEntity<StoreVisitorDTO> getVisitorById(
            @PathVariable Long storeId,
            @PathVariable Long visitorId,
            Authentication authentication) {
        
        StoreVisitorDTO visitor = storeVisitorService.getVisitorById(visitorId);
        return ResponseEntity.ok(visitor);
    }

    /**
     * Get visitor statistics for a store
     *
     * @param storeId The store ID
     * @param authentication The authenticated user
     * @return The visitor statistics
     */
    @GetMapping("/stats")
    @RequiresStoreRole(value = com.olatech.shopxauthservice.Model.StoreRole.StoreRoleType.STAFF)
    public ResponseEntity<StoreVisitorService.VisitorStats> getVisitorStats(
            @PathVariable Long storeId,
            Authentication authentication) {
        
        StoreVisitorService.VisitorStats stats = storeVisitorService.getVisitorStats(storeId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Track a visitor (create or update)
     * This endpoint is public and doesn't require authentication
     *
     * @param storeId The store ID
     * @param visitorDTO The visitor data
     * @param request The HTTP request (for IP and user agent)
     * @return The created or updated visitor
     */
    @PostMapping("/track")
    public ResponseEntity<StoreVisitorDTO> trackVisitor(
            @PathVariable Long storeId,
            @RequestBody StoreVisitorDTO visitorDTO,
            HttpServletRequest request) {
        
        // Extract IP address and user agent from request
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        StoreVisitorDTO createdVisitor = storeVisitorService.trackVisitor(storeId, visitorDTO, ipAddress, userAgent);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVisitor);
    }

    /**
     * Update a visitor
     *
     * @param storeId The store ID
     * @param visitorId The visitor ID
     * @param visitorDTO The updated visitor data
     * @param authentication The authenticated user
     * @return The updated visitor
     */
    @PutMapping("/{visitorId}")
    @RequiresStoreRole(value = com.olatech.shopxauthservice.Model.StoreRole.StoreRoleType.STAFF)
    public ResponseEntity<StoreVisitorDTO> updateVisitor(
            @PathVariable Long storeId,
            @PathVariable Long visitorId,
            @RequestBody StoreVisitorDTO visitorDTO,
            Authentication authentication) {
        
        StoreVisitorDTO updatedVisitor = storeVisitorService.updateVisitor(visitorId, visitorDTO);
        return ResponseEntity.ok(updatedVisitor);
    }

    /**
     * Delete a visitor
     *
     * @param storeId The store ID
     * @param visitorId The visitor ID
     * @param authentication The authenticated user
     * @return Empty response with 204 status
     */
    @DeleteMapping("/{visitorId}")
    @RequiresStoreRole(value = com.olatech.shopxauthservice.Model.StoreRole.StoreRoleType.MANAGER)
    public ResponseEntity<Void> deleteVisitor(
            @PathVariable Long storeId,
            @PathVariable Long visitorId,
            Authentication authentication) {
        
        storeVisitorService.deleteVisitor(visitorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the client's IP address from the request
     *
     * @param request The HTTP request
     * @return The client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
