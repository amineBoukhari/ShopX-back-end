package com.olatech.shopxauthservice.security;

import com.olatech.shopxauthservice.Model.StoreRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify that a method requires a specific store role for access.
 * Used with the AuthorizationAspect to enforce role-based access control.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresStoreRole {
    /**
     * The minimum required role for the store
     */
    StoreRole.StoreRoleType value();
    
    /**
     * The parameter name containing the storeId (defaults to "storeId")
     */
    String storeIdParam() default "storeId";
}
