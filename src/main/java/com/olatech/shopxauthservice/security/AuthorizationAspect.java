package com.olatech.shopxauthservice.security;

import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Service.AuthorizationService;
import com.olatech.shopxauthservice.Service.UserService;
import com.olatech.shopxauthservice.exceptions.UnauthorizedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Aspect for handling authorization annotations.
 * This class intercepts method calls annotated with authorization annotations
 * and enforces the appropriate access controls before allowing the method to execute.
 */
@Aspect
@Component
public class AuthorizationAspect {
    
    @Autowired
    private AuthorizationService authorizationService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Intercepts methods annotated with @RequiresStoreRole and validates
     * that the current user has the required role for the store.
     *
     * @param joinPoint The intercepted method call
     * @param requiresStoreRole The annotation on the method
     */
    @Before("@annotation(requiresStoreRole)")
    public void checkStoreRole(JoinPoint joinPoint, RequiresStoreRole requiresStoreRole) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = method.getParameters();
        
        // Extract store ID from method arguments
        Long storeId = extractLongParam(args, parameters, requiresStoreRole.storeIdParam());
        if (storeId == null) {
            throw new IllegalArgumentException("Store ID parameter not found: " + requiresStoreRole.storeIdParam());
        }
        
        // Extract authentication from method arguments
        Authentication authentication = extractAuthentication(args);
        if (authentication == null) {
            throw new UnauthorizedException("Authentication required");
        }
        
        // Get current user and validate store role
        Users currentUser = userService.getUserByUsername(authentication.getName());
        authorizationService.validateStoreRole(currentUser, storeId, requiresStoreRole.value());
    }
    
    /**
     * Intercepts methods annotated with @RequiresResourceAccess and validates
     * that the current user has the required access to the resource.
     *
     * @param joinPoint The intercepted method call
     * @param requiresResourceAccess The annotation on the method
     */
    @Before("@annotation(requiresResourceAccess)")
    public void checkResourceAccess(JoinPoint joinPoint, RequiresResourceAccess requiresResourceAccess) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = method.getParameters();
        
        // Extract resource ID from method arguments
        Long resourceId = extractLongParam(args, parameters, requiresResourceAccess.resourceIdParam());
        if (resourceId == null) {
            throw new IllegalArgumentException("Resource ID parameter not found: " + requiresResourceAccess.resourceIdParam());
        }
        
        // Extract authentication from method arguments
        Authentication authentication = extractAuthentication(args);
        if (authentication == null) {
            throw new UnauthorizedException("Authentication required");
        }
        
        // Get current user and validate resource access
        Users currentUser = userService.getUserByUsername(authentication.getName());
        authorizationService.validateResourceAccess(
                currentUser,
                requiresResourceAccess.resourceType(),
                resourceId,
                requiresResourceAccess.action()
        );
    }
    
    /**
     * Extracts a Long parameter value from method arguments.
     *
     * @param args The method arguments
     * @param parameters The method parameters
     * @param paramName The name of the parameter to extract
     * @return The Long value, or null if not found
     */
    private Long extractLongParam(Object[] args, Parameter[] parameters, String paramName) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (param.getName().equals(paramName)) {
                Object value = args[i];
                if (value instanceof Long) {
                    return (Long) value;
                } else if (value instanceof Integer) {
                    return ((Integer) value).longValue();
                } else if (value instanceof String) {
                    return Long.parseLong((String) value);
                }
            }
        }
        return null;
    }
    
    /**
     * Extracts the Authentication object from method arguments.
     *
     * @param args The method arguments
     * @return The Authentication object, or null if not found
     */
    private Authentication extractAuthentication(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Authentication) {
                return (Authentication) arg;
            }
        }
        return null;
    }
}
