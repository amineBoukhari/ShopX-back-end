package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.analysis.ProductSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSuggestionRepository extends JpaRepository<ProductSuggestion, Long> {
}