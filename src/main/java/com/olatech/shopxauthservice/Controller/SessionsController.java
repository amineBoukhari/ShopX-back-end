package com.olatech.shopxauthservice.Controller;

import com.olatech.shopxauthservice.DTO.LoginResponse;
import com.olatech.shopxauthservice.DTO.RefreshTokenResponse;
import com.olatech.shopxauthservice.Model.Sessions;
import com.olatech.shopxauthservice.Model.UserPrincipal;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Service.JWTService;
import com.olatech.shopxauthservice.Service.SessionService;
import com.olatech.shopxauthservice.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@CrossOrigin
public class SessionsController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @GetMapping("/sessions")
    public ResponseEntity<List<Sessions>> getAllSessions(Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        List<Sessions> sessions = sessionService.getAllSessionsByUserId(user.getUser());
        return ResponseEntity.ok(sessions);
    }
    
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<?> deleteSession(
            @PathVariable Long id,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        
        // Vérifier que la session appartient à l'utilisateur
        Sessions session = sessionService.getSessionById(id);
        if (session == null || !session.getUser().equals(user.getUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Session non trouvée ou non autorisée");
        }
        
        sessionService.deleteSession(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/sessions/all-except-current")
    public ResponseEntity<?> deleteAllSessionsExceptCurrent(
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Users user = userPrincipal.getUser();
        
        // Extraire le token du header d'autorisation
        String token = authHeader.substring(7); // Retirer "Bearer "
        
        // Trouver la session courante
        Optional<Sessions> currentSession = sessionService.findByToken(token);
        
        if (currentSession.isPresent()) {
            sessionService.deleteAllSessionsExceptCurrent(user, currentSession.get().getId());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session courante non trouvée");
        }
    }
    
    @GetMapping("/sessions/current")
    public ResponseEntity<?> getCurrentSession(
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader) {
        // Extraire le token du header d'autorisation
        String token = authHeader.substring(7); // Retirer "Bearer "
        log.info("Token: {}", token);
        
        // Trouver la session courante
        Optional<Sessions> currentSession = sessionService.findByToken(token);
        log.info("Current session: {}", currentSession.toString());
        
        if (currentSession.isPresent()) {
            // Mettre à jour l'horodatage de dernière activité
            Sessions session = sessionService.updateLastActivityTime(token);
            System.out.println("Session: " + session);
            return ResponseEntity.ok(currentSession);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session courante non trouvée");
        }
    }

    @PostMapping("/refresh")
    public RefreshTokenResponse refreshToken(@RequestBody String token) throws Exception {
        try {
            System.out.println("token" + token);
            String username = jwtService.extractUserName(token);
            if (username == null) {
                return null;
            }

            Users user = userService.getUserByUsername(username);
            if (user == null) {
                return null;
            }

            String newToken = jwtService.generateToken(user);
            return new RefreshTokenResponse(newToken);
        }
        catch (Exception e) {
            System.out.println("Error refreshing token" + e.getMessage());
            return null;
        }
    }
}
