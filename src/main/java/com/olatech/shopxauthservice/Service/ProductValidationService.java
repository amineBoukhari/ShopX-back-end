package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.Model.*;
import com.olatech.shopxauthservice.Repository.ProductFieldDefinitionRepository;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class ProductValidationService {

    @Autowired
    private ProductFieldDefinitionRepository fieldDefRepository;

    @Autowired
    private VariantFieldDefinitionRepository variantDefRepository;

    public void validateProduct(Product product) {
        ProductType type = product.getProductType();

        // Charger les définitions des champs pour ce type
        List<ProductFieldDefinition> requiredFields =
                fieldDefRepository.findByProductTypeAndRequiredTrue(type);

        // Vérifier les champs requis
        for (ProductFieldDefinition fieldDef : requiredFields) {
            validateField(product, fieldDef);
        }

        // Vérifier les variantes si nécessaire
        if (product.HasVariants()) {
            List<VariantFieldDefinition> variantDefs =
                    variantDefRepository.findByProductType(type);

            for (ProductVariant variant : product.getVariants()) {
                validateVariant(variant, variantDefs);
            }
        }
    }

    private void validateField(Product product, ProductFieldDefinition fieldDef) {
            Optional<ProductFieldValue> fieldValue = product.getFieldValues().stream()
                .filter(f -> f.getFieldName().equals(fieldDef.getFieldName()))
                .findFirst();

        if (fieldDef.isRequired() && fieldValue.isEmpty()) {
            throw new ValidationException(
                    String.format("Required field '%s' is missing", fieldDef.getFieldName())
            );
        }

        if (fieldValue.isPresent()) {
            String value = fieldValue.get().getValue();
            validateFieldValue(value, fieldDef);
        }
    }

    private void validateFieldValue(String value, ProductFieldDefinition fieldDef) {
        switch (fieldDef.getFieldType()) {
            case "NUMBER":
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new ValidationException(
                            String.format("Invalid number format for field '%s'", fieldDef.getFieldName())
                    );
                }
                break;

            case "BOOLEAN":
                if (!value.equals("true") && !value.equals("false")) {
                    throw new ValidationException(
                            String.format("Invalid boolean value for field '%s'", fieldDef.getFieldName())
                    );
                }
                break;
            default:
                // Pas de validation spécifique pour les autres types

        }
    }

    private void validateVariant(
            ProductVariant variant,
            List<VariantFieldDefinition> variantDefs
    ) {
        for (VariantFieldDefinition def : variantDefs) {
            String value = variant.getOptionValues().get(def.getOptionName());

            if (def.isRequired() && (value == null || value.trim().isEmpty())) {
                log.info("Required variant option '{}' is missing for variant '{}'",
                        def.getOptionName(), variant.toString());
                throw new ValidationException(
                        String.format("Required variant option '%s' is missing",
                                def.getOptionName())
                );
            }

            if (value != null && !def.getAllowedValues().contains(value)) {
                throw new ValidationException(
                        String.format("Invalid value '%s' for option '%s'. Allowed values: %s",
                                value, def.getOptionName(),
                                String.join(", ", def.getAllowedValues()))
                );
            }
        }
    }
}