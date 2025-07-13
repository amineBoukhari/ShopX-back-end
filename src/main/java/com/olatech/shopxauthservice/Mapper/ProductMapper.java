package com.olatech.shopxauthservice.Mapper;

import com.olatech.shopxauthservice.DTO.Product.CreateProductDto;
import com.olatech.shopxauthservice.DTO.VariantDTO;
import com.olatech.shopxauthservice.Model.*;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {LocalDateTime.class})
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", source = "sku")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "basePrice", source = "basePrice", defaultValue = "0")
    @Mapping(target = "salePrice", source = "salePrice", defaultValue = "0")
    @Mapping(target = "active", source = "active", defaultValue = "false")
    @Mapping(target = "manageStock", source = "manageStock", defaultValue = "false")
    @Mapping(target = "stockThreshold", source = "stockThreshold", defaultValue = "0")
    @Mapping(target = "hasVariants", source = "hasVariants")
    @Mapping(target = "fieldValues", expression = "java(mapFieldValues(dto))")
    @Mapping(target = "images", expression = "java(mapImages(dto.imageUrls))")
    @Mapping(target = "variants", expression = "java(mapVariants(dto.variants))")
    @Mapping(target = "tags", expression = "java(mapTags(dto.tags))")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    Product toEntity(CreateProductDto dto);

    @Named("mapFieldValues")
    default Set<ProductFieldValue> mapFieldValues(CreateProductDto dto) {
        if (dto.fieldNames == null || dto.fieldValues == null) {
            return new HashSet<>();
        }

        return IntStream.range(0, dto.fieldNames.length)
                .mapToObj(i -> {
                    ProductFieldValue fieldValue = new ProductFieldValue();
                    fieldValue.setFieldName(dto.fieldNames[i]);
                    fieldValue.setValue(dto.fieldValues[i]);
                    return fieldValue;
                })
                .collect(Collectors.toSet());
    }

    @Named("mapImages")
    default List<ProductImage> mapImages(String[] imageUrls) {
        if (imageUrls == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(imageUrls)
                .map(url -> {
                    ProductImage image = new ProductImage();
                    image.setImageUrl(url);
                    return image;
                })
                .collect(Collectors.toList());
    }

    @Named("mapVariants")
    default Set<ProductVariant> mapVariants(VariantDTO[] variantDTOs) {
        if (variantDTOs == null) {
            return new HashSet<>();
        }

        return Arrays.stream(variantDTOs)
                .map(dto -> {
                    ProductVariant variant = new ProductVariant();
                    variant.setName(dto.getName());
                    variant.setBasePrice(new BigDecimal(dto.getBasePrice() != null ? dto.getBasePrice() : "0"));
                    variant.setSalePrice(new BigDecimal(dto.getSalePrice() != null ? dto.getSalePrice() : "0"));
                    variant.setManageStock(dto.getManageStock());
                    variant.setStockThreshold(dto.getStockThreshold() != null ? dto.getStockThreshold() : 0);
                    variant.setActive(dto.isActive());
                    variant.setOptionValues(dto.getOptionValues());
                    variant.setCreatedAt(LocalDateTime.now());

                    // Map variant images using getImages() instead of getImageUrls()
                    if (dto.getImages() != null) {
                        variant.setImages(dto.getImages().stream()
                                .map(url -> {
                                    VariantImage image = new VariantImage();
                                    image.setImageUrl(url);
                                    image.setVariant(variant);
                                    return image;
                                })
                                .collect(Collectors.toList()));
                    } else {
                        variant.setImages(new ArrayList<>());
                    }

                    return variant;
                })
                .collect(Collectors.toSet());
    }
    @Named("mapTags")
    default Set<String> mapTags(String[] tags) {
        if (tags == null) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(tags));
    }

    @AfterMapping
    default void establishRelationships(@MappingTarget Product product) {
        // Set bidirectional relationships for field values
        product.getFieldValues().forEach(fieldValue -> fieldValue.setProduct(product));

        // Set bidirectional relationships for images
        product.getImages().forEach(image -> image.setProduct(product));

        // Set bidirectional relationships for variants
        product.getVariants().forEach(variant -> {
            variant.setProduct(product);
            variant.getImages().forEach(image -> image.setVariant(variant));
        });
    }
}