package com.olatech.shopxauthservice.Service;


import com.olatech.shopxauthservice.Model.ProductType;
import com.olatech.shopxauthservice.Model.VariantFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantFieldDefinitionRepository extends JpaRepository<VariantFieldDefinition, Long> {
    List<VariantFieldDefinition> findByProductType(ProductType type);
    List<VariantFieldDefinition> findByProductTypeAndRequiredTrue(ProductType type);
    List<VariantFieldDefinition> findByProductTypeOrderByOrderPosition(ProductType type);
}