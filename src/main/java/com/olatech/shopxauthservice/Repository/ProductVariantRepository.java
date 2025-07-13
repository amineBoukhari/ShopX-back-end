package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.Product;
import com.olatech.shopxauthservice.Model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProduct(Product product);
}
