# ShopX Authorization Testing Documentation

## Test Coverage Overview

The testing strategy for the ShopX authorization system focuses on ensuring that the role-based access control (RBAC) system correctly enforces permissions according to the defined role hierarchy. The primary test suite is `AuthorizationServiceTest` which validates the core authorization logic.

## AuthorizationServiceTest

### Purpose
This test class validates that the `AuthorizationService` correctly implements the role-based access control system, ensuring that:

1. Users can only access resources they have permission for
2. The role hierarchy is properly enforced (OWNER > ADMIN > MANAGER > STAFF)
3. Authorization errors are correctly handled and reported

### Test Setup

The test suite creates a complete test environment with:

- A set of test users with different roles:
  - Owner user
  - Admin user
  - Manager user
  - Staff user
  - Unauthorized user (no roles)
- A test store entity
- Store role associations connecting users to the store with appropriate roles
- Mocked dependencies for repositories and services

```java
@BeforeEach
void setUp() {
    // Setup test users with different roles
    ownerUser = new Users();
    ownerUser.setId(1);
    ownerUser.setUsername("owner");
    
    // Additional user setup
    
    // Setup test store
    testStore = new Store();
    testStore.setId(1L);
    testStore.setName("Test Store");
    testStore.setOwner(ownerUser);
    
    // Setup store roles
    ownerRole = new StoreRole();
    ownerRole.setStore(testStore);
    ownerRole.setUser(ownerUser);
    ownerRole.setRole(StoreRole.StoreRoleType.OWNER);
    
    // Additional role setup
}
```

### Test Categories

#### 1. Role Hierarchy Tests

These tests verify that the role hierarchy is correctly enforced:

- `testHasStoreRole_Owner_Success`: Confirms owners have access to owner-level operations
- `testHasStoreRole_Admin_AccessToAdminLevel`: Verifies admins have admin-level access
- `testHasStoreRole_Admin_NoAccessToOwnerLevel`: Ensures admins cannot perform owner-level operations
- `testHasStoreRole_Manager_AccessToStaffLevel`: Confirms managers inherit staff permissions
- `testHasStoreRole_Staff_NoAccessToManagerLevel`: Verifies staff cannot perform manager operations

Example test:
```java
@Test
void testHasStoreRole_Admin_NoAccessToOwnerLevel() {
    // Arrange
    when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
    when(storeRoleRepository.findByUserAndStore(adminUser, testStore)).thenReturn(Optional.of(adminRole));

    // Act
    boolean result = authorizationService.hasStoreRole(adminUser, 1L, StoreRole.StoreRoleType.OWNER);

    // Assert
    assertFalse(result, "Admin should not have OWNER access");
    verify(storeRepository).findById(1L);
    verify(storeRoleRepository).findByUserAndStore(adminUser, testStore);
}
```

#### 2. Authorization Validation Tests

These tests verify the authorization validation methods work correctly:

- `testValidateStoreRole_Success`: Confirms the validation passes for authorized users
- `testValidateStoreRole_InsufficientPermissions`: Verifies proper exception throwing for unauthorized access
- `testValidateStoreRole_StoreNotFound`: Tests handling of missing resources

Example test:
```java
@Test
void testValidateStoreRole_InsufficientPermissions() {
    // Arrange
    when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
    when(storeRoleRepository.findByUserAndStore(staffUser, testStore)).thenReturn(Optional.of(staffRole));

    // Act & Assert
    UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
        authorizationService.validateStoreRole(staffUser, 1L, StoreRole.StoreRoleType.ADMIN);
    });
    assertEquals("Insufficient permissions for store with ID: 1", exception.getMessage());
}
```

#### 3. JWT Permission Mapping Tests

These tests verify the correct mapping of roles to permissions for JWT tokens:

- `testGetUserPermissions`: Validates that roles are correctly mapped to permission strings

```java
@Test
void testGetUserPermissions() {
    // Arrange
    List<StoreRole> roles = new ArrayList<>();
    roles.add(ownerRole);
    
    StoreRole secondStoreRole = new StoreRole();
    Store secondStore = new Store();
    secondStore.setId(2L);
    secondStoreRole.setStore(secondStore);
    secondStoreRole.setUser(ownerUser);
    secondStoreRole.setRole(StoreRole.StoreRoleType.MANAGER);
    roles.add(secondStoreRole);
    
    when(storeRoleRepository.findByUser(ownerUser)).thenReturn(roles);

    // Act
    List<String> permissions = authorizationService.getUserPermissions(ownerUser);

    // Assert
    assertEquals(2, permissions.size());
    assertTrue(permissions.contains("store:1:full_access"));
    assertTrue(permissions.contains("store:2:edit"));
}
```

#### 4. Authentication Integration Tests

These tests verify the integration with Spring Security's Authentication system:

- `testGetCurrentUser`: Verifies retrieval of the current user from Authentication
- `testGetCurrentUser_AuthenticationNull`: Tests handling of null Authentication

```java
@Test
void testGetCurrentUser() {
    // Arrange
    when(authentication.getName()).thenReturn("owner");
    when(userService.getUserByUsername("owner")).thenReturn(ownerUser);

    // Act
    Users user = authorizationService.getCurrentUser(authentication);

    // Assert
    assertEquals(ownerUser, user);
    verify(userService).getUserByUsername("owner");
}
```

## Running the Tests

### Using Maven

To run all tests:
```bash
./mvnw test
```

To run only the authorization tests:
```bash
./mvnw test -Dtest=AuthorizationServiceTest
```

### Using an IDE

In IntelliJ IDEA or Eclipse:
1. Right-click on the test class
2. Select "Run 'AuthorizationServiceTest'"

## Test Coverage

The current test suite covers:

- 95% of `AuthorizationService` methods
- All core permission checking logic
- Error handling and exceptional cases
- Integration with Spring Security components

## Future Test Enhancements

Planned future test enhancements include:

1. Integration tests for the `AuthorizationAspect` to verify annotation-based security
2. Controller tests with security context to verify end-to-end authorization
3. Performance tests for authorization checks under load
4. Additional tests for complex role combinations and edge cases