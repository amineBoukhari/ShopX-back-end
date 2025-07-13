# ShopX Authorization Implementation Guide

This document provides a comprehensive guide for implementing the centralized authorization system in the ShopX application. It includes step-by-step instructions, component descriptions, and troubleshooting information.

## Table of Contents

1. [Implementation Overview](#implementation-overview)
2. [Component Architecture](#component-architecture)
3. [Implementation Steps](#implementation-steps)
4. [Configuration Changes](#configuration-changes)
5. [Common Issues and Solutions](#common-issues-and-solutions)
6. [Testing and Validation](#testing-and-validation)

## Implementation Overview

The authorization system is built around a centralized `AuthorizationService` and uses aspect-oriented programming to apply authorization rules declaratively via annotations. This approach ensures:

- Consistent authorization across all endpoints
- Clear separation between business logic and security concerns
- Easy maintenance and extension of security rules

## Component Architecture

The authorization system consists of these key components:

1. **AuthorizationService**: Core service containing all authorization logic
2. **Custom Annotations**: Declarative security markers for endpoints
3. **AuthorizationAspect**: Intercepts method calls and enforces security
4. **Enhanced JWT Tokens**: Contains detailed authorization information
5. **Updated Security Configuration**: Properly secures all endpoints

## Implementation Steps

### 1. Create Authorization Annotations

Create two annotation interfaces in the `security` package:

- `RequiresStoreRole`: Specifies the required store role level
- `RequiresResourceAccess`: Defines resource-level access control

### 2. Implement the Authorization Service

Create the `AuthorizationService` class that implements:

- Role hierarchy validation
- Resource access checking
- Helper methods for JWT token generation

### 3. Implement the Authorization Aspect

Create an aspect that intercepts calls to annotated methods and enforces:

- Proper role access for store operations
- Resource-specific permissions
- Authentication state

### 4. Update the JWT Service

Enhance the JWT service to include detailed permission information in tokens:

- Add store role mappings
- Include permission strings
- Link to the AuthorizationService

### 5. Update Controllers and Services

Replace in-line authorization checks with annotations:

- Add `@RequiresStoreRole` to controller methods
- Remove redundant access checks in service methods
- Use centralized `getCurrentUser` method

### 6. Update Security Configuration

Refine the security configuration to:

- Protect sensitive endpoints
- Fix overly permissive configurations
- Apply proper authentication requirements

## Configuration Changes

### Maven Dependencies

Add the AspectJ dependency to enable aspect-oriented programming:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### Application Properties

No specific properties required, but you may want to add:

```properties
# Enable AspectJ auto-proxy
spring.aop.auto=true
spring.aop.proxy-target-class=true
```

## Common Issues and Solutions

### Circular Dependencies

If you encounter circular dependency errors:

```
The dependencies of some of the beans in the application context form a cycle
```

Solution:
1. Add `@Lazy` annotation to one of the dependencies:
   ```java
   @Autowired
   @Lazy
   private AuthorizationService authorizationService;
   ```
2. Import `org.springframework.context.annotation.Lazy`

### Missing Authorization Checks

If authorization is not being enforced on annotated methods:

1. Verify AspectJ is properly enabled
2. Check annotation syntax and parameters
3. Ensure aspect intercepts the correct pointcuts

### JWT Token Issues

If enhanced tokens are not working:

1. Verify token structure in debug logs
2. Check permissions generation logic
3. Ensure client correctly parses permission data

## Testing and Validation

### Unit Testing

Run the provided `AuthorizationServiceTest` to validate:

- Role hierarchy enforcement
- Permission mapping
- Error handling

### Manual Testing

Test key endpoints with different user roles:

1. Log in as different user types (owner, admin, manager, staff)
2. Attempt operations requiring different permission levels
3. Verify appropriate access and denial responses

### Security Audit

Regularly audit the system for:

- Missing annotations on sensitive endpoints
- Proper role assignments for operations
- JWT token security and permission accuracy

## Conclusion

This implementation provides a robust, maintainable authorization system that enforces proper access controls throughout the ShopX application. By centralizing authorization logic and using a declarative approach, we ensure consistent security while keeping the codebase clean and maintainable.