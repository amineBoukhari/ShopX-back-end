package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.DTO.InvitationDTO;
import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Model.StoreInvitation;
import com.olatech.shopxauthservice.Model.StoreRole;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Repository.StoreInvitationRepository;
import com.olatech.shopxauthservice.Repository.StoreRepository;
import com.olatech.shopxauthservice.Repository.StoreRoleRepository;
import com.olatech.shopxauthservice.Repository.UserRepo;
import com.olatech.shopxauthservice.exceptions.ResourceNotFoundException;
import com.olatech.shopxauthservice.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// StoreInvitationService.java
@Service
public class StoreInvitationService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private StoreInvitationRepository invitationRepository;

    @Autowired
    private StoreRoleRepository storeRoleRepository;

    @Transactional
    public StoreInvitation createInvitation(Long storeId, InvitationDTO invitationDTO, Users inviter) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        if (!hasManagementAccess(store, inviter)) {
            throw new UnauthorizedException("No permission to create invitations");
        }

        // Vérifier si l'utilisateur existe déjà
        Users invitedUser = userRepository.findByEmail(invitationDTO.getEmail())
                .orElse(null);

        StoreInvitation invitation = new StoreInvitation();
        invitation.setStore(store);
        invitation.setEmail(invitationDTO.getEmail());
        invitation.setRole(invitationDTO.getRole());
        invitation.setInviter(inviter);
        invitation.setStatus(StoreInvitation.InvitationStatus.PENDING);

        return invitationRepository.save(invitation);
    }

    public List<StoreInvitation> getPendingInvitations(Long storeId, Users user) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        if (!hasManagementAccess(store, user)) {
            throw new UnauthorizedException("No permission to view invitations");
        }

        return invitationRepository.findByStoreAndStatus(store, StoreInvitation.InvitationStatus.PENDING);
    }

    public List<StoreInvitation> getInvitationsByUser(String email) {
        return invitationRepository.findByEmail(email);
    }

    @Transactional
    public void acceptInvitation(Long invitationId, Users user) {
        StoreInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getEmail().equals(user.getEmail())) {
            throw new UnauthorizedException("This invitation is not for you");
        }
        if (invitation.getStatus() != StoreInvitation.InvitationStatus.PENDING) {
            throw new UnauthorizedException("This invitation is not pending");
        }
        invitation.setStatus(StoreInvitation.InvitationStatus.ACCEPTED);
        invitation.getStore().getStaff().add(user);

        // Créer le rôle pour le nouveau membre
        StoreRole staffRole = new StoreRole();
        staffRole.setStore(invitation.getStore());
        staffRole.setUser(user);
        staffRole.setRole(invitation.getRole());
        storeRoleRepository.save(staffRole);

        invitationRepository.save(invitation);
    }

    public boolean hasManagementAccess(Store store, Users user) {
        StoreRole userRole = storeRoleRepository.findByStoreAndUser(store, user)
                .orElse(null);

        if (userRole == null) return false;

        return userRole.getRole() == StoreRole.StoreRoleType.OWNER ||
                userRole.getRole() == StoreRole.StoreRoleType.ADMIN;
    }

    public void rejectInvitation(Long invitationId, Users currentUser) {
        StoreInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getEmail().equals(currentUser.getEmail())) {
            throw new UnauthorizedException("This invitation is not for you");
        }

        invitation.setStatus(StoreInvitation.InvitationStatus.DECLINED);
        invitationRepository.save(invitation);
    }

    public StoreInvitation getInvitationById(Long invitationId) {
        return invitationRepository.findById(invitationId)
                .orElse(null);
    }

    public void deleteInvitation(Long storeId, Long invitationId, Users currentUser) {
        StoreInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!hasManagementAccess(invitation.getStore(), currentUser)) {
            throw new UnauthorizedException("No permission to delete invitation");
        }

        invitationRepository.delete(invitation);
    }
}
