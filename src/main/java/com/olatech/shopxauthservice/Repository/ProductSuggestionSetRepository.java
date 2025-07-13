package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.analysis.ProductSuggestionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSuggestionSetRepository extends JpaRepository<ProductSuggestionSet, Long> {
    Optional<ProductSuggestionSet> findTopByProductIdOrderByCreatedAtDesc(Long productId);

    boolean existsByProductIdAndStatus(Long productId, ProductSuggestionSet.AnalysisStatus status);

    List<ProductSuggestionSet> findByProductIdOrderByCreatedAtDesc(Long productId);
}