package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.Product;
import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByStore(Store store, Pageable pageable);
    List<Product> findByStore(Store store);
    List<Product> findByCategory(Category category);
    Optional<Product> findBySlugAndStore(String slug, Store store);
    List<Product> findByIsActiveAndStore(boolean isActive, Store store);
    boolean existsBySlugAndStore(String slug, Store store);

    long countByStoreAndIsActive(Store store, boolean b);

    boolean existsBySku(String sku);


    Page<Product> findByStoreAndNameContainingIgnoreCase(Store store, String searchTerm, Pageable pageable);

}