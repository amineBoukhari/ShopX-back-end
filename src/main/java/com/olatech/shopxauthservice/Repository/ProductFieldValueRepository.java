package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.Product;
import com.olatech.shopxauthservice.Model.ProductFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductFieldValueRepository extends JpaRepository<ProductFieldValue, Long> {
    List<ProductFieldValue> findByProduct(Product product);
    void deleteByProduct(Product product);
}