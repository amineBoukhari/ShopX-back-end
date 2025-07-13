package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.StoreRole;
import com.olatech.shopxauthservice.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// StoreRoleRepository.java
public interface StoreRoleRepository extends JpaRepository<StoreRole, Long> {
    Optional<StoreRole> findByStoreAndUser(Store store, Users user);
    Optional<StoreRole> findByStoreAndUser_Id(Store store, int user);
    List<StoreRole> findByUser(Users user);

    Optional<StoreRole> findByUserAndStore(Users user, Store store);
}
