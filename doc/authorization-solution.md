# Centralized Authorization Mechanism for ShopX

Based on the analysis of the shopx-auth-service, we implemented a centralized authorization mechanism to improve security and maintainability. This approach ensures consistent authorization checks across all endpoints and provides a flexible framework for defining and enforcing access rules.

## Solution: Authorization Service Pattern

### 1. Authorization Service

A dedicated `AuthorizationService` centralizes all authorization logic:

```java
@Service
public class AuthorizationService {
    
    @Autowired
    private StoreRoleRepository storeRoleRepository;
    
    /**
     * Check if a user has at least the specified role for a store
     */
    public boolean hasStoreRole(Users user, Long storeId, StoreRole.StoreRoleType minimumRole) {
        // Implementation details
    }
    
    /**
     * Check if a user is the owner of a store
     */
    public boolean isStoreOwner(Users user, Long storeId) {
        // Implementation details
    }
    
    /**
     * Check if a user has access to a specific resource
     */
    public boolean hasResourceAccess(Users user, String resourceType, Long resourceId, String action) {
        // Implementation details
    }
}
```

### 2. Authorization Aspect

Aspect-oriented programming for cross-cutting authorization concerns:

```java
@Aspect
@Component
public class AuthorizationAspect {
    
    @Autowired
    private AuthorizationService authorizationService;
    
    @Before("@annotation(requiresStoreRole)")
    public void checkStoreRole(JoinPoint joinPoint, RequiresStoreRole requiresStoreRole) {
        // Implementation details
    }
}
```

### 3. Custom Annotations

Annotations to declaratively specify authorization requirements:

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresStoreRole {
    StoreRole.StoreRoleType role();
    String storeIdParam() default "storeId";
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresResourceAccess {
    String resourceType();
    String resourceIdParam();
    String action();
}
```

### 4. Implementation in Controllers

Example controller with annotation-based authorization:

```java
@RestController
public class StoreController {
    
    @GetMapping("/api/stores/{storeId}")
    @RequiresStoreRole(role = StoreRole.StoreRoleType.STAFF)
    public ResponseEntity<Store> getStore(@PathVariable Long storeId, Authentication authentication) {
        // The aspect will verify authorization before this code runs
    }
    
    @PutMapping("/api/stores/{storeId}")
    @RequiresStoreRole(role = StoreRole.StoreRoleType.MANAGER)
    public ResponseEntity<Store> updateStore(@PathVariable Long storeId, ...) {
        // The aspect will verify authorization before this code runs
    }
    
    @DeleteMapping("/api/stores/{storeId}")
    @RequiresStoreRole(role = StoreRole.StoreRoleType.OWNER)
    public ResponseEntity<?> deleteStore(@PathVariable Long storeId, ...) {
        // The aspect will verify authorization before this code runs
    }
}
```

### 5. JWT Token Enhancement

Enhanced JWT token to include detailed authorization information:

```java
public String generateToken(Users user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    
    // Include role information for each store
    List<StoreRole> storeRoles = storeRoleRepository.findByUser(user);
    List<Map<String, Object>> storeAccess = storeRoles.stream().map(storeRole -> {
        Map<String, Object> store = new HashMap<>();
        store.put("storeId", storeRole.getStore().getId());
        store.put("role", storeRole.getRole());
        return store;
    }).collect(Collectors.toList());
    
    claims.put("stores", storeAccess);
    
    // Include user permissions
    claims.put("permissions", getUserPermissions(user));
    
    // JWT creation logic
}
```

### 6. Update Security Configuration

Refined the SecurityConfig to properly protect endpoints:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.
            authorizeHttpRequests(request -> request.
                    requestMatchers("/login", "/register", "/oauth2/**", "/swagger-ui/**", 
                                   "/v3/api-docs/**", "/swagger-ui.html", "/refresh").
                    permitAll().
                    requestMatchers("/api/invitations/{invitationId}/accept", "/api/invitations/{invitationId}/reject").
                    authenticated().  // Require authentication for invitation actions
                    anyRequest().
                    authenticated()).
            // rest of configuration
}
```

## Benefits of This Approach

1. **Centralized Logic**: All authorization rules are defined in one service, making them easier to update and maintain.

2. **Declarative Security**: Annotations make security requirements clear at the controller method level.

3. **Separation of Concerns**: Authorization logic is separated from business logic.

4. **Consistent Application**: Authorization checks are applied consistently across all endpoints.

5. **Enhanced Auditing**: Centralized authorization facilitates comprehensive security logging and auditing.

6. **Simplified Testing**: Authorization rules can be tested in isolation.

7. **Flexible Permission Model**: The system can easily accommodate new roles and permission types.