package com.olatech.shopxauthservice.Controller.Exposed;

import com.olatech.shopxauthservice.DTO.StoreVisitorDTO;
import com.olatech.shopxauthservice.Service.StoreVisitorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public controller for tracking store visitors
 * This controller is accessible without authentication
 */
@RestController
@RequestMapping("/public-api/stores/{storeId}/visitors")
@RequiredArgsConstructor
public class PublicVisitorController {

    private final StoreVisitorService storeVisitorService;

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
     * Update newsletter subscription for a visitor
     *
     * @param storeId The store ID
     * @param email The visitor's email
     * @param subscribed Whether the visitor is subscribed to the newsletter
     * @return Success response
     */
    @PostMapping("/newsletter")
    public ResponseEntity<Object> updateNewsletterSubscription(
            @PathVariable Long storeId,
            @RequestParam String email,
            @RequestParam Boolean subscribed) {
        
        // Create a minimal DTO with just the necessary fields
        StoreVisitorDTO visitorDTO = new StoreVisitorDTO();
        visitorDTO.setEmail(email);
        visitorDTO.setNewsletter(subscribed);
        visitorDTO.setStoreId(storeId);
        
        // Use the track method which handles both creating new and updating existing visitors
        storeVisitorService.trackVisitor(storeId, visitorDTO, null, null);
        
        return ResponseEntity.ok().body(
            Map.of(
                "success", true,
                "message", subscribed ? "Successfully subscribed to newsletter" : "Successfully unsubscribed from newsletter"
            )
        );
    }

    /**
     * Record a page view for a visitor
     *
     * @param storeId The store ID
     * @param userId The visitor's user ID
     * @param pageUrl The page URL being visited
     * @param request The HTTP request (for IP and user agent)
     * @return Success response
     */
    @PostMapping("/pageview")
    public ResponseEntity<Object> trackPageView(
            @PathVariable Long storeId,
            @RequestParam String userId,
            @RequestParam String pageUrl,
            HttpServletRequest request) {
        
        // Extract IP address and user agent from request
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        // Create a minimal DTO with just the necessary fields
        StoreVisitorDTO visitorDTO = new StoreVisitorDTO();
        visitorDTO.setUserId(userId);
        visitorDTO.setLastPageVisited(pageUrl);
        visitorDTO.setStoreId(storeId);
        
        storeVisitorService.trackVisitor(storeId, visitorDTO, ipAddress, userAgent);
        
        return ResponseEntity.ok().body(
            Map.of(
                "success", true,
                "message", "Page view recorded successfully"
            )
        );
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
