package com.olatech.shopxauthservice.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify that a method requires access to a specific resource type.
 * Used with the AuthorizationAspect to enforce resource-based access control.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresResourceAccess {
    /**
     * The type of resource being accessed (e.g., "store", "product", "invitation")
     */
    String resourceType();
    
    /**
     * The parameter name containing the resource ID
     */
    String resourceIdParam();
    
    /**
     * The action being performed on the resource (e.g., "view", "edit", "delete")
     */
    String action();
}
