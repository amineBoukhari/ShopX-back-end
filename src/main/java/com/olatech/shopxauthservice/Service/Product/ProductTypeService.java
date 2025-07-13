package com.olatech.shopxauthservice.Service.Product;

import com.olatech.shopxauthservice.DTO.Product.ProductTypeDTO;
import com.olatech.shopxauthservice.Mapper.Product.ProductTypeMapper;
import com.olatech.shopxauthservice.Model.ProductType;
import com.olatech.shopxauthservice.Repository.ProductTypeRepository;
import com.olatech.shopxauthservice.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductTypeService {
    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductTypeMapper productTypeMapper;

    public List<ProductTypeDTO> getAllProductTypes() {
        List<ProductType> productTypes = productTypeRepository.findAll();
        return productTypeMapper.toDTOs(productTypes);
    }

    public ProductTypeDTO getProductTypeById(Long id) {
        ProductType productType = productTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type de produit non trouvé avec l'id : " + id));
        return productTypeMapper.toDTO(productType);
    }

    public ProductType getProductTypeById1(Long id) {
        return productTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type de produit non trouvé avec l'id : " + id));
    }

}
