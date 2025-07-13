package com.olatech.shopxauthservice.config;

import com.olatech.shopxauthservice.Model.Sessions;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Repository.SessionRepo;
import com.olatech.shopxauthservice.Repository.UserRepo;
import com.olatech.shopxauthservice.Service.JWTService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JWTService jwtService;
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private SessionRepo sessionRepo;
    
    @Value("${app.oauth2.frontend-redirect-uri:http://localhost:3000/auth/callback}")
    private String frontendRedirectUri;
    
    @Value("${app.cookie.domain:localhost}")
    private String cookieDomain;
    
    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;
    
    @Value("#{'${app.oauth2.allowed-redirect-hosts:http://localhost:3000,http://192.168.1.42:3000}'.split(',')}")
    private List<String> allowedRedirectHosts;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        
        log.info("==== OAuth2 SUCCESS HANDLER TRIGGERED ====");
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Authentication class: {}", authentication.getClass().getName());
        
        if (!(authentication.getPrincipal() instanceof OAuth2User)) {
            log.error("Principal is not an OAuth2User instance, it is: {}", 
                    authentication.getPrincipal() != null ? 
                            authentication.getPrincipal().getClass().getName() : "null");
            response.sendRedirect(frontendRedirectUri + "?auth_error=invalid_principal");
            return;
        }
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        // Log all user attributes for debugging
        log.info("OAuth2 user attributes:");
        for (Map.Entry<String, Object> entry : oauth2User.getAttributes().entrySet()) {
            log.info("  {} = {}", entry.getKey(), entry.getValue());
        }
        
        // Récupérer l'email pour identifier l'utilisateur
        String email = oauth2User.getAttribute("email");
        if (email == null) {
            log.error("User email not found in OAuth2 attributes");
            response.sendRedirect(frontendRedirectUri + "?auth_error=email_not_found");
            return;
        }
        
        log.info("User email from OAuth2: {}", email);
        
        // Récupérer l'utilisateur
        Users user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            log.error("User not found for email: {}", email);
            response.sendRedirect(frontendRedirectUri + "?auth_error=user_not_found");
            return;
        }
        
        log.info("User found in database: id={}, username={}", user.getId(), user.getUsername());
        
        // Générer les tokens JWT
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        
        log.info("JWT tokens generated successfully");
        
        // Sauvegarder la session
        Sessions session = new Sessions();
        session.setUser(user);
        session.setToken(jwtService.extractTokenId(accessToken));
        session.setRefreshToken(jwtService.extractTokenId(refreshToken));
        session.setExpiresAt(jwtService.extractExpiration(accessToken));
        session.setCreatedAt(new Date());
        session.setUserAgent(request.getHeader("User-Agent"));
        session.setIpAddress(getClientIP(request));
        sessionRepo.save(session);
        
        log.info("Session saved to database with ID: {}", session.getId());

        Date expiry = jwtService.extractExpiration(accessToken);
        log.info("Token expiry: {}", expiry);
        
        // Ajouter les tokens dans des cookies sécurisés
        addTokenCookie(response, "token", accessToken, jwtService.extractExpiration(accessToken));
        addTokenCookie(response, "refreshToken", refreshToken, jwtService.extractExpiration(refreshToken));
        
        log.info("Auth cookies added to response");
        
        // Determine the redirect path based on profile completion status
        String baseRedirectPath = user.isProfileCompleted() ? 
                frontendRedirectUri : 
                frontendRedirectUri.replace("/auth/callback", "/auth/complete-profile");
        
        // Create the basic URI builder with the base path
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseRedirectPath);
        
        // Manually encode query parameters that might contain special characters
        try {
            // Add simple parameters
            builder.queryParam("auth_success", "true");
            builder.queryParam("user_id", user.getId());
            builder.queryParam("username", user.getUsername());
            builder.queryParam("email", user.getEmail());
            builder.queryParam("profileCompleted", user.isProfileCompleted());
            
            // Manually encode parameters that might contain spaces or special characters
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            
            String encodedFirstName = URLEncoder.encode(firstName, StandardCharsets.UTF_8);
            String encodedLastName = URLEncoder.encode(lastName, StandardCharsets.UTF_8);
            
            builder.queryParam("firstName", encodedFirstName);
            builder.queryParam("lastName", encodedLastName);
            
            // Use URLEncoder for JWT tokens too as they contain special characters
            builder.queryParam("token", URLEncoder.encode(accessToken, StandardCharsets.UTF_8));
            builder.queryParam("refreshToken", URLEncoder.encode(refreshToken, StandardCharsets.UTF_8));
            
            // Build the URL (without additional encoding since we've already done it)
            String targetUrl = builder.build().toUriString();
            
            log.info("Built redirect URL: {}", targetUrl);
            
            // Vérifier que l'URL de redirection est autorisée pour des raisons de sécurité
            if (!isRedirectUriAllowed(targetUrl)) {
                log.error("Redirect URI not allowed: {}", targetUrl);
                response.sendRedirect(frontendRedirectUri + "?auth_error=invalid_redirect");
                return;
            }
            
            log.info("OAuth2 authentication complete, redirecting user to frontend");
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            log.error("Error building redirect URL", e);
            response.sendRedirect(frontendRedirectUri + "?auth_error=redirect_error&error_message=" + 
                    URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }
    
    /**
     * Vérifie que l'URI de redirection est autorisée pour des raisons de sécurité
     */
    private boolean isRedirectUriAllowed(String uri) {
        try {
            // Extract the base URL (scheme, host, port) without query parameters
            // This avoids parsing issues with query parameters
            String baseUri = uri;
            int queryIndex = uri.indexOf('?');
            if (queryIndex > 0) {
                baseUri = uri.substring(0, queryIndex);
            }
            
            URI redirectUri = new URI(baseUri);
            String redirectHost = redirectUri.getScheme() + "://" + redirectUri.getHost() + 
                    (redirectUri.getPort() > 0 ? ":" + redirectUri.getPort() : "");
            
            boolean isAllowed = allowedRedirectHosts.stream()
                    .anyMatch(allowedHost -> redirectHost.equals(allowedHost.trim()));
                    
            log.info("Checking if redirect host is allowed: {} - Result: {}", redirectHost, isAllowed);
            return isAllowed;
        } catch (URISyntaxException e) {
            log.error("Invalid redirect URI: {}", uri, e);
            return false;
        }
    }
    
    /**
     * Ajoute un cookie sécurisé contenant un token
     */
    private void addTokenCookie(HttpServletResponse response, String name, String value, Date expiry) {
        log.debug("Generating cookie: name={}, value={}, expiry={}", name, value, expiry);
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setDomain(cookieDomain);
        cookie.setAttribute("SameSite", "Lax");
        //age de 30 jours
        cookie.setMaxAge(30 * 24 * 60 * 60);
        
        if (expiry != null) {
            // Cookie expiry logic here if needed
        }
        
        response.addCookie(cookie);
        log.debug("Added cookie: name={}, secure={}, httpOnly={}, path={}, domain={}, maxAge={}",
                name, cookie.getSecure(), cookie.isHttpOnly(), cookie.getPath(), 
                cookie.getDomain(), cookie.getMaxAge());
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
