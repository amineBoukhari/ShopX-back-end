package com.olatech.shopxauthservice.Controller;

import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Repository.UserRepo;
import com.olatech.shopxauthservice.Service.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Contrôleur pour les opérations liées à l'authentification
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private JWTService jwtService;
    
    @Value("${app.oauth2.frontend-redirect-uri}")
    private String frontendRedirectUri;

    /**
     * Endpoint pour vérifier l'état d'authentification de l'utilisateur
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && 
                                 authentication.isAuthenticated() &&
                                 !authentication.getPrincipal().equals("anonymousUser");
        
        log.info("Auth status check - isAuthenticated: {}", isAuthenticated);
        
        return ResponseEntity.ok().body(new AuthStatusResponse(isAuthenticated));
    }
    
    /**
     * Endpoint de callback pour les redirections OAuth2 via le frontend
     * Cette méthode peut être utilisée si le frontend a besoin de rediriger vers le backend
     * pour compléter le processus d'authentification.
     */
    @GetMapping("/callback")
    public RedirectView handleAuthCallback(HttpServletRequest request) {
        log.info("Auth callback received from frontend");
        
        // Construire l'URL de redirection vers le frontend
        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("callback_processed", "true")
                .build().toUriString();
        
        return new RedirectView(targetUrl);
    }
    
    /**
     * DTO for profile completion request
     */
    public static class CompleteProfileRequest {
        private String firstName;
        private String lastName;
        private String email;
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    /**
     * Response for profile completion
     */
    public static class CompleteProfileResponse {
        private final boolean success;
        private final String message;
        private final int userId;
        
        public CompleteProfileResponse(boolean success, String message, int userId) {
            this.success = success;
            this.message = message;
            this.userId = userId;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public int getUserId() {
            return userId;
        }
    }
    
    /**
     * Endpoint to complete user profile information after OAuth2 login
     */
    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(@RequestBody CompleteProfileRequest request,
                                           HttpServletRequest httpRequest) {
        // Extract user information from JWT token
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(new CompleteProfileResponse(
                false, "Unauthorized: No token provided", 0));
        }
        
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        
        if (username == null) {
            return ResponseEntity.status(401).body(new CompleteProfileResponse(
                false, "Unauthorized: Invalid token", 0));
        }
        
        // Get user from database
        Users user = userRepo.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(new CompleteProfileResponse(
                false, "User not found", 0));
        }
        
        // Update user information
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        // Only update email if it's different and not already taken
        if (!user.getEmail().equals(request.getEmail())) {
            // Check if email is already taken
            boolean emailExists = userRepo.findByEmail(request.getEmail()).isPresent();
            if (emailExists) {
                return ResponseEntity.status(400).body(new CompleteProfileResponse(
                    false, "Email already in use", user.getId()));
            }
            user.setEmail(request.getEmail());
        }
        
        // Mark profile as completed
        user.setProfileCompleted(true);
        
        // Save user
        userRepo.save(user);
        
        return ResponseEntity.ok(new CompleteProfileResponse(
            true, "Profile completed successfully", user.getId()));
    }
    
    /**
     * Classe de réponse pour l'état d'authentification
     */
    private static class AuthStatusResponse {
        private final boolean authenticated;
        
        public AuthStatusResponse(boolean authenticated) {
            this.authenticated = authenticated;
        }
        
        public boolean isAuthenticated() {
            return authenticated;
        }
    }
}
