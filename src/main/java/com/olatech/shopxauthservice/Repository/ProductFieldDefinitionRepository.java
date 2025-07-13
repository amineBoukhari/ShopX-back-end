package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.ProductFieldDefinition;
import com.olatech.shopxauthservice.Model.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductFieldDefinitionRepository extends JpaRepository<ProductFieldDefinition, Long> {
    List<ProductFieldDefinition> findByProductType(ProductType type);
    List<ProductFieldDefinition> findByProductTypeAndRequiredTrue(ProductType type);
    List<ProductFieldDefinition> findByProductTypeOrderByOrderPosition(ProductType type);
}