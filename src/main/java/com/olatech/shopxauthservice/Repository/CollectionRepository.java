package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.Collection;
import com.olatech.shopxauthservice.Model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByStore(Store store);
    List<Collection> findByStoreId(Long storeId);
}