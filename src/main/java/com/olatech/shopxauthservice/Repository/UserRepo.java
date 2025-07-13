package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.Users;
import jakarta.validation.constraints.Email;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepo extends JpaRepository <Users, Long> {

    Optional<Users> findByUsername(String username);

    Optional<Users> findByEmail(@Email String email);

    boolean existsByEmail(@Email String email);

    boolean existsByUsername(String username);

    void deleteByUsername(String username);
    
    // Trouver un utilisateur par provider et providerId
    Optional<Users> findByProviderAndProviderId(String provider, String providerId);

}
