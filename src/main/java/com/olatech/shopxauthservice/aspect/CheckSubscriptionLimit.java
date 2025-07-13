package com.olatech.shopxauthservice.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer les méthodes qui nécessitent une vérification
 * des limites du plan d'abonnement avant leur exécution.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CheckSubscriptionLimit {
    
    /**
     * Type de limite à vérifier (ex: PRODUCT, API_CALL, STORAGE)
     */
    LimitType type();
    
    /**
     * Indique si la méthode va incrémenter le compteur de ressources
     */
    boolean increment() default true;
    
    /**
     * Paramètre contenant l'ID du store (si différent de "storeId" ou "store.id")
     */
    String storeIdParam() default "";
    
    /**
     * Le nombre d'unités à ajouter/vérifier (utile pour les opérations en lot)
     */
    int amount() default 1;
}
