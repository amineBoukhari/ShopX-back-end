package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.DTO.StoreVisitorDTO;
import com.olatech.shopxauthservice.Model.FrontStore.StoreVisitor;
import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Repository.StoreRepository;
import com.olatech.shopxauthservice.Repository.StoreVisitorRepository;
import com.olatech.shopxauthservice.exceptions.ResourceNotFoundException;
import com.olatech.shopxauthservice.Mapper.StoreVisitorMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing store visitors
 */
@Service
@RequiredArgsConstructor
public class StoreVisitorService {

    private final StoreVisitorRepository storeVisitorRepository;
    private final StoreRepository storeRepository;
    private final StoreVisitorMapper storeVisitorMapper;

    // Utiliser le mapper pour la conversion

    /**
     * Get all visitors for a specific store
     * 
     * @param storeId The store ID
     * @return List of StoreVisitorDTOs
     */
    public List<StoreVisitorDTO> getAllVisitors(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));
        
        return storeVisitorRepository.findByStoreOrderByCreatedAtDesc(store)
                .stream()
                .map(storeVisitorMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a visitor by ID
     * 
     * @param visitorId The visitor ID
     * @return The StoreVisitorDTO
     */
    public StoreVisitorDTO getVisitorById(Long visitorId) {
        StoreVisitor visitor = storeVisitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor not found with id: " + visitorId));
        
        return storeVisitorMapper.toDto(visitor);
    }

    /**
     * Track a new visitor or update an existing one
     * 
     * @param storeId The store ID
     * @param visitorDTO The visitor data
     * @param ipAddress The visitor's IP address
     * @param userAgent The visitor's user agent
     * @return The created or updated StoreVisitorDTO
     */
    @Transactional
    public StoreVisitorDTO trackVisitor(Long storeId, StoreVisitorDTO visitorDTO, String ipAddress, String userAgent) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        String userId = visitorDTO.getUserId();
        if (userId == null || userId.isEmpty()) {
            // Generate a unique user ID if not provided
            userId = UUID.randomUUID().toString();
        }

        // Try to find an existing visitor
        Optional<StoreVisitor> existingVisitor;
        if (visitorDTO.getEmail() != null && !visitorDTO.getEmail().isEmpty()) {
            existingVisitor = storeVisitorRepository.findByEmailAndStore(visitorDTO.getEmail(), store);
        } else {
            existingVisitor = storeVisitorRepository.findByUserIdAndStore(userId, store);
        }

        StoreVisitor visitor;
        if (existingVisitor.isPresent()) {
            // Update existing visitor
            visitor = existingVisitor.get();
            visitor.setLastPageVisited(visitorDTO.getLastPageVisited());
            visitor.setTotalVisits(visitor.getTotalVisits() != null ? visitor.getTotalVisits() + 1 : 1);
            visitor.setTotalPageViews(visitor.getTotalPageViews() != null ? visitor.getTotalPageViews() + 1 : 1);
            
            // Update other fields if provided
            if (visitorDTO.getFirstName() != null) visitor.setFirstName(visitorDTO.getFirstName());
            if (visitorDTO.getLastName() != null) visitor.setLastName(visitorDTO.getLastName());
            if (visitorDTO.getPhone() != null) visitor.setPhone(visitorDTO.getPhone());
            if (visitorDTO.getDiscoverySource() != null) visitor.setDiscoverySource(visitorDTO.getDiscoverySource());
            if (visitorDTO.getNewsletter() != null) visitor.setNewsletter(visitorDTO.getNewsletter());
            if (visitorDTO.getSource() != null) visitor.setSource(visitorDTO.getSource());
        } else {
            // Create new visitor
            visitor = new StoreVisitor();
            visitor.setUserId(userId);
            visitor.setStore(store);
            visitor.setEmail(visitorDTO.getEmail());
            visitor.setFirstName(visitorDTO.getFirstName());
            visitor.setLastName(visitorDTO.getLastName());
            visitor.setPhone(visitorDTO.getPhone());
            visitor.setDiscoverySource(visitorDTO.getDiscoverySource());
            visitor.setNewsletter(visitorDTO.getNewsletter() != null ? visitorDTO.getNewsletter() : false);
            visitor.setSource(visitorDTO.getSource());
            visitor.setLastPageVisited(visitorDTO.getLastPageVisited());
            visitor.setTotalVisits(1);
            visitor.setTotalPageViews(1);
        }

        // Always update IP and user agent
        visitor.setIpAddress(ipAddress);
        visitor.setUserAgent(userAgent);

        visitor = storeVisitorRepository.save(visitor);
        return storeVisitorMapper.toDto(visitor);
    }

    /**
     * Update visitor information
     * 
     * @param visitorId The visitor ID
     * @param visitorDTO The updated visitor data
     * @return The updated StoreVisitorDTO
     */
    @Transactional
    public StoreVisitorDTO updateVisitor(Long visitorId, StoreVisitorDTO visitorDTO) {
        StoreVisitor visitor = storeVisitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor not found with id: " + visitorId));
        
        // Utiliser le mapper pour la mise à jour contrôlée de l'entité
        storeVisitorMapper.updateEntityFromDto(visitorDTO, visitor);
        
        visitor = storeVisitorRepository.save(visitor);
        return storeVisitorMapper.toDto(visitor);
    }

    /**
     * Delete a visitor
     * 
     * @param visitorId The visitor ID
     */
    @Transactional
    public void deleteVisitor(Long visitorId) {
        if (!storeVisitorRepository.existsById(visitorId)) {
            throw new ResourceNotFoundException("Visitor not found with id: " + visitorId);
        }
        storeVisitorRepository.deleteById(visitorId);
    }

    /**
     * Get visitor statistics for a store
     * 
     * @param storeId The store ID
     * @return Statistics containing total visitors, recent visitors, etc.
     */
    public VisitorStats getVisitorStats(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));
        
        Long totalVisitors = storeVisitorRepository.countVisitorsByStoreId(storeId);
        
        // Get visitors for the last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Long recentVisitors = storeVisitorRepository.countVisitorsByStoreIdAndDateRange(storeId, thirtyDaysAgo, LocalDateTime.now());
        
        // Get newsletter subscribers
        Long newsletterSubscribers = (long) storeVisitorRepository.findByStoreAndNewsletterTrue(store).size();
        
        return new VisitorStats(totalVisitors, recentVisitors, newsletterSubscribers);
    }

    /**
     * Inner class to represent visitor statistics
     */
    @Data
    @AllArgsConstructor
    public static class VisitorStats {
        private Long totalVisitors;
        private Long recentVisitors; // Last 30 days
        private Long newsletterSubscribers;
    }
}
