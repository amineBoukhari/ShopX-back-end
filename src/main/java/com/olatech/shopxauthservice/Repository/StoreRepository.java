package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.StoreRole;
import com.olatech.shopxauthservice.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByOwnerOrStaffContaining(Users owner, Users staff);
    
    Optional<Store> findBySubdomain(String subdomain);
    
    Optional<Store> findBySlug(String slug);
    
    boolean existsBySubdomain(String subdomain);
}

