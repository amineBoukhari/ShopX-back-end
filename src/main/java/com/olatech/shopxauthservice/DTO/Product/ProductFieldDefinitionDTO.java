package com.olatech.shopxauthservice.DTO.Product;

import lombok.Data;

@Data
public class ProductFieldDefinitionDTO {
    private Long id;
    private String fieldName;
    private String fieldType;
    private boolean required;
    private int orderPosition;
    private String regex;
    private String minValue;
    private String maxValue;
    private boolean multipleValues;
}