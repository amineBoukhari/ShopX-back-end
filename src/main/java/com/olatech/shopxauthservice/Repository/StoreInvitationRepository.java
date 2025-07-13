package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.StoreInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreInvitationRepository extends JpaRepository<StoreInvitation, Long> {
    List<StoreInvitation> findByStoreAndStatus(Store store, StoreInvitation.InvitationStatus status);
    Optional<StoreInvitation> findByStoreAndEmail(Store store, String email);

    List<StoreInvitation> findByEmail(String email);
}