package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.FrontStore.StoreVisitor;
import com.olatech.shopxauthservice.Model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreVisitorRepository extends JpaRepository<StoreVisitor, Long> {
    
    /**
     * Find a store visitor by user ID and store
     * @param userId The unique user ID
     * @param store The store
     * @return Optional containing the store visitor if found
     */
    Optional<StoreVisitor> findByUserIdAndStore(String userId, Store store);
    
    /**
     * Find all visitors for a specific store
     * @param store The store
     * @return List of store visitors
     */
    List<StoreVisitor> findByStore(Store store);
    
    /**
     * Find all visitors for a specific store ordered by creation date (newest first)
     * @param store The store
     * @return List of store visitors
     */
    List<StoreVisitor> findByStoreOrderByCreatedAtDesc(Store store);
    
    /**
     * Find visitors by email and store
     * @param email The email address
     * @param store The store
     * @return Optional containing the store visitor if found
     */
    Optional<StoreVisitor> findByEmailAndStore(String email, Store store);
    
    /**
     * Get total number of visitors for a store
     * @param storeId The store ID
     * @return The count of visitors
     */
    @Query("SELECT COUNT(v) FROM StoreVisitor v WHERE v.store.id = :storeId")
    Long countVisitorsByStoreId(@Param("storeId") Long storeId);
    
    /**
     * Get total number of visitors for a store within a date range
     * @param storeId The store ID
     * @param startDate The start date
     * @param endDate The end date
     * @return The count of visitors in the date range
     */
    @Query("SELECT COUNT(v) FROM StoreVisitor v WHERE v.store.id = :storeId AND v.createdAt BETWEEN :startDate AND :endDate")
    Long countVisitorsByStoreIdAndDateRange(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find visitors that have subscribed to the newsletter for a specific store
     * @param store The store
     * @return List of store visitors with newsletter subscription
     */
    List<StoreVisitor> findByStoreAndNewsletterTrue(Store store);
    
    /**
     * Find visitors that came from a specific source
     * @param store The store
     * @param source The traffic source
     * @return List of store visitors from the specified source
     */
    List<StoreVisitor> findByStoreAndSource(Store store, String source);
}
