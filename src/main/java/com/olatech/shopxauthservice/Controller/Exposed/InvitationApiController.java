package com.olatech.shopxauthservice.Controller.Exposed;

import com.olatech.shopxauthservice.Model.StoreInvitation;
import com.olatech.shopxauthservice.Repository.UserRepo;
import com.olatech.shopxauthservice.Service.JWTService;
import com.olatech.shopxauthservice.Service.StoreInvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
public class InvitationApiController {

    @Autowired
    private StoreInvitationService invitationService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/validate")
    public ResponseEntity<?> validateInvitation(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");

        try {
            // Extraire l'ID d'invitation du token
            Long invitationId = jwtService.extractInvitationId(token);
            if (invitationId == null) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Token d'invitation invalide"
                ));
            }

            // Récupérer l'invitation
            StoreInvitation invitation = invitationService.getInvitationById(invitationId);
            if (invitation == null) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Cette invitation n'existe pas"
                ));
            }

            // Vérifier le statut
            if (invitation.getStatus() != StoreInvitation.InvitationStatus.PENDING) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Cette invitation a déjà été " +
                                (invitation.getStatus() == StoreInvitation.InvitationStatus.ACCEPTED ? "acceptée" :
                                        invitation.getStatus() == StoreInvitation.InvitationStatus.DECLINED ? "refusée" :
                                                "expirée")
                ));
            }

            // Vérifier si l'invitation est expirée
            if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
                invitation.setStatus(StoreInvitation.InvitationStatus.EXPIRED);
                //invitationService.saveInvitation(invitation);
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Cette invitation a expiré"
                ));
            }

            // Vérifier si l'utilisateur existe déjà
            boolean userExists = userRepo.findByEmail(invitation.getEmail()).isPresent();

            // Retourner les infos de l'invitation
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("valid", true);
            responseData.put("userExists", userExists);
            responseData.put("data", Map.of(
                    "invitationId", invitation.getId(),
                    "email", invitation.getEmail(),
                    "role", invitation.getRole().toString(),
                    "storeName", invitation.getStore().getName(),
                    "inviterName", invitation.getInviter().getUsername(),
                    "expiresAt", invitation.getExpiresAt()
            ));

            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Erreur lors de la validation: " + e.getMessage()
            ));
        }
    }

}