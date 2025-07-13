package com.olatech.shopxauthservice.DTO.Product;

import lombok.Data;

import java.util.List;

@Data
public class ProductTypeDTO {
    private Long id;
    private String name;
    private String slug;
    private List<ProductFieldDefinitionDTO> fields;

    public ProductTypeDTO() {
    }

    public ProductTypeDTO(Long id, String name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }
}