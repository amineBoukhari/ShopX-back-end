package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.ProductType;
import com.olatech.shopxauthservice.Model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

}
