package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.Category;
import com.olatech.shopxauthservice.Model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findAllRootCategories();

    List<Category> findByParentId(Long parentId);

    boolean existsBySlug(String slug);

    @Query("SELECT DISTINCT c FROM Category c " +
            "LEFT JOIN FETCH c.subcategories s " +
            "LEFT JOIN FETCH s.subcategories " +
            "WHERE c.parent IS NULL " +
            "ORDER BY c.id")
    List<Category> findAllRootCategoriesWithSubcategories();

    @Query(value = """
        WITH RECURSIVE CategoryHierarchy AS (
            -- Sélectionner les catégories racines
            SELECT c.*, 0 as level
            FROM category c
            WHERE c.parent_id IS NULL
            
            UNION ALL
            
            -- Sélectionner récursivement les sous-catégories
            SELECT child.*, ch.level + 1
            FROM category child
            INNER JOIN CategoryHierarchy ch ON child.parent_id = ch.id
        )
        SELECT *
        FROM CategoryHierarchy
        ORDER BY level, id
        """,
            nativeQuery = true)
    List<Category> findAllWithHierarchy();

}