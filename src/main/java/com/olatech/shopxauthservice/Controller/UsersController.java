package com.olatech.shopxauthservice.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olatech.shopxauthservice.DTO.LoginBody;
import com.olatech.shopxauthservice.DTO.LoginResponse;
import com.olatech.shopxauthservice.Model.Sessions;
import com.olatech.shopxauthservice.Model.StoreInvitation;
import com.olatech.shopxauthservice.Model.UserPrincipal;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Service.*;
import com.olatech.shopxauthservice.exceptions.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin
public class UsersController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    UserService userService;

    @Autowired
    SessionService sessionService;

    @Autowired
    JWTService jwtService;

    @Autowired
   private LocalFileStorageService localFileStorageService;

    private final OAuth2AuthorizedClientService clientService;

    @Autowired
    public UsersController(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    @Autowired
    private StoreInvitationService invitationService;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody Users user) {
        try {
            Users createdUser = userService.register(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "status", "success",
                            "message", "User created successfully",
                            "data", createdUser
                    )
            );
        } catch (UserAlreadyExistsException e) {
            // Gestion des conflits (HTTP 409 Conflict)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of(
                            "status", "error",
                            "message", e.getMessage(),
                            "field", e.getField()
                    )
            );
        } catch (Exception e) {
            // Gestion des erreurs internes (HTTP 500)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "status", "error",
                            "message", e.getMessage()
                    )
            );
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginBody user, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String identifier;

            // Résolution dynamique de l'identifiant en fonction du mode
            switch (user.getMode()) {
                case "email":
                    if (user.getEmail() == null || user.getEmail().isBlank()) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Email is required for email login mode"));
                    }
                    identifier = user.getEmail();
                    break;
                case "username":
                default:
                    if (user.getUsername() == null || user.getUsername().isBlank()) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Username is required for username login mode"));
                    }
                    identifier = user.getUsername();
                    break;
            }

            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(identifier, user.getPassword()));
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Users authenticatedUser = userPrincipal.getUser();

            // Générer les tokens JWT (comme dans OAuth2)
            String accessToken = jwtService.generateToken(authenticatedUser);
            String refreshToken = jwtService.generateRefreshToken(userPrincipal.getUsername());

            // Créer et configurer la session (comme dans OAuth2)
            Sessions session = new Sessions();
            session.setUser(authenticatedUser);
            session.setToken(jwtService.extractTokenId(accessToken));
            session.setRefreshToken(jwtService.extractTokenId(refreshToken));
            session.setExpiresAt(jwtService.extractExpiration(accessToken));
            session.setCreatedAt(new Date());
            session.setUserAgent(request.getHeader("User-Agent"));
            session.setIpAddress(getClientIP(request));
            session.setLastActivityTime(new Date());

            // Sauvegarder la session
            sessionService.createSession(session);

            // Ajouter les tokens dans des cookies sécurisés (comme dans OAuth2)

            return ResponseEntity.status(HttpStatus.OK).body(
                    new LoginResponse(
                            accessToken,
                            refreshToken)
            );
        }
        catch (Exception e) {
            System.out.println("User not authenticated: " + e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of(
                            "status", "error",
                            "message", e.getMessage()
                    )
            );
        }
    }

    @GetMapping("/me")
    public ResponseEntity me(Authentication authentication) {
    System.out.println(authentication.getPrincipal());

        //print the type of authentication
        log.info("Authentication type: " + authentication.getPrincipal().getClass().getName());
        System.out.println(authentication.getPrincipal().getClass().getName());

        if (authentication.getPrincipal() instanceof UserPrincipal user) {
            log.info("User found: " + user.getUsername());
            return ResponseEntity.ok(user.getUser());
        }

        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User user = (OAuth2User) authentication.getPrincipal();
            return ResponseEntity.ok(user.getAttributes());
        }
        return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of(
                        "status", "error",
                        "message", "User not found"
                )
        );
    }

    @GetMapping("/")
    public String home(Authentication authentication) {

        return  "'Welcome to the home page " + authentication.getName();
    }


    @GetMapping("/invitations")
    public ResponseEntity getInvitations(Authentication authentication){
        Users currentUser = userService.getUserByUsername(authentication.getName());
        List<StoreInvitation> invitations = invitationService.getInvitationsByUser(currentUser.getEmail());
        return ResponseEntity.ok(invitations);
    }

    @PostMapping("/api/invitations/{invitationId}/accept")
    public ResponseEntity<?> acceptInvitation(
            @PathVariable Long invitationId,
            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        invitationService.acceptInvitation(invitationId, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/invitations/{invitationId}/reject")
    public ResponseEntity<?> rejectInvitation(
            @PathVariable Long invitationId,
            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        invitationService.rejectInvitation(invitationId, currentUser);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/api/users/profile", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> updateProfile(
            @RequestPart(value = "userData", required = false) String userJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @ModelAttribute Users userRaw,
            Authentication authentication
    ) {
        try {
            Users currentUser = userService.getUserByUsername(authentication.getName());
            Users user;

            // Cas 1 : multipart/form-data avec userData en JSON string
            if (userJson != null) {
                ObjectMapper mapper = new ObjectMapper();
                user = mapper.readValue(userJson, Users.class);
            }
            // Cas 2 : application/json
            else if (userRaw != null) {
                user = userRaw;
            } else {
                throw new IllegalArgumentException("Aucune donnée utilisateur fournie.");
            }

            currentUser.setFirstName(user.getFirstName());
            currentUser.setLastName(user.getLastName());
            currentUser.setEmail(user.getEmail());
            currentUser.setUsername(user.getUsername());

            if (avatar != null && !avatar.isEmpty()) {
                String avatarUrl = localFileStorageService.uploadFile(avatar);
                currentUser.setImageUrl(avatarUrl);
            }

            Users updatedUser = userService.updateUser(currentUser);
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }


    @DeleteMapping("/api/users/account")
    public ResponseEntity<?> deleteAcount(Authentication authentication) {

        userService.deletUserByusername(authentication.getName());
        return ResponseEntity.ok().build();
    }


    /**
     * Récupère l'adresse IP du client en tenant compte des proxys
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
