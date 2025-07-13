package com.olatech.shopxauthservice.Controller;


import com.olatech.shopxauthservice.DTO.Product.ProductTypeDTO;
import com.olatech.shopxauthservice.Model.ProductType;
import com.olatech.shopxauthservice.Service.Product.ProductTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/{storeID}/product-type")
@Tag(name = "Product Type", description = "Product Type API")
public class ProductTypeController {
    @Autowired
    private ProductTypeService productTypeService;

    @RequestMapping("/all")
    @Operation(summary = "Get all product types", description = "Get all product types")
    public ResponseEntity<List<ProductTypeDTO>> getAllProductTypes() {
        return ResponseEntity.ok(productTypeService.getAllProductTypes());
    }

    @RequestMapping("/{id}")
    @Operation(summary = "Récupérer un type de produit par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Type de produit trouvé"),
            @ApiResponse(responseCode = "404", description = "Type de produit non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<ProductTypeDTO> getProductTypeById(
            @Parameter(description = "Product type id")
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(productTypeService.getProductTypeById(id));
    }
}
