package com.olatech.shopxauthservice.Mapper.Product;

import com.olatech.shopxauthservice.DTO.Product.ProductFieldDefinitionDTO;
import com.olatech.shopxauthservice.DTO.Product.ProductTypeDTO;
import com.olatech.shopxauthservice.Model.ProductFieldDefinition;
import com.olatech.shopxauthservice.Model.ProductType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductTypeMapper {
    @Mapping(source = "fields", target = "fields")
    ProductTypeDTO toDTO(ProductType productType);
    List<ProductTypeDTO> toDTOs(List<ProductType> productTypes);

    ProductFieldDefinitionDTO toDTO(ProductFieldDefinition fieldDefinition);
}