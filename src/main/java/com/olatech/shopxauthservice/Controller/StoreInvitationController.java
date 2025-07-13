package com.olatech.shopxauthservice.Controller;

import com.olatech.shopxauthservice.DTO.InvitationDTO;
import com.olatech.shopxauthservice.Model.StoreInvitation;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Service.EmailService;
import com.olatech.shopxauthservice.Service.StoreInvitationService;
import com.olatech.shopxauthservice.Service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// StoreInvitationController.java
@RestController
@RequestMapping("/api/stores/{storeId}/invitations")
public class StoreInvitationController {

    @Autowired
    private StoreInvitationService invitationService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<StoreInvitation> createInvitation(
            @PathVariable Long storeId,
            @Valid @RequestBody InvitationDTO invitationDTO,
            Authentication authentication)  {
        try {
            Users currentUser = userService.getUserByUsername(authentication.getName());
            StoreInvitation invitation = invitationService.createInvitation(storeId, invitationDTO, currentUser);
            emailService.sendInvitationEmail(invitation);
            return ResponseEntity.ok(invitation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<StoreInvitation>> getPendingInvitations(
            @PathVariable Long storeId,
            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        List<StoreInvitation> invitations = invitationService.getPendingInvitations(storeId, currentUser);
        return ResponseEntity.ok(invitations);
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<?> deleteInvitation(
            @PathVariable Long storeId,
            @PathVariable Long invitationId,
            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        invitationService.deleteInvitation(storeId, invitationId, currentUser);
        return ResponseEntity.ok().build();
    }


}
