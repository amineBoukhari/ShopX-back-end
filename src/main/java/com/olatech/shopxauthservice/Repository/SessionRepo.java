package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.Sessions;
import com.olatech.shopxauthservice.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepo extends JpaRepository<Sessions, Long> {
    List<Sessions> findByUser(Users user);


    Optional<Sessions> findByRefreshToken(String tokenId);
}
