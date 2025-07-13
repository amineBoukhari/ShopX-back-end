package com.olatech.shopxauthservice.Controller;

import com.olatech.shopxauthservice.DTO.CollectionDTO;
import com.olatech.shopxauthservice.Model.Collection;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Service.CollectionService;
import com.olatech.shopxauthservice.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/collections")
@Tag(name = "Collections", description = "Collection management APIs")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private UserService userService;

    @Operation(
            summary = "Create a new collection",
            description = "Creates a new collection in the specified store. Requires store manager permissions."
    )
    @PostMapping
    public ResponseEntity<Collection> createCollection(
            @PathVariable Long storeId,
            @Valid @RequestBody CollectionDTO collectionDTO,
            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        collectionDTO.setStoreId(storeId);
        Collection collection = collectionService.createCollection(collectionDTO, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(collection);
    }

    @Operation(
            summary = "Get all collections",
            description = "Retrieves all collections for the specified store"
    )
    @GetMapping
    public ResponseEntity<List<Collection>> getStoreCollections(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId,
            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        List<Collection> collections = collectionService.getStoreCollections(storeId, currentUser);
        return ResponseEntity.ok(collections);
    }

    @Operation(
            summary = "Get collection by ID",
            description = "Retrieves a specific collection by its ID"
    )
    @GetMapping("/{collectionId}")
    public ResponseEntity<Collection> getCollection(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId,
            @Parameter(description = "Collection ID", required = true)
            @PathVariable Long collectionId,
            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        Collection collection = collectionService.getCollectionById(collectionId, currentUser);
        return ResponseEntity.ok(collection);
    }

    @Operation(
            summary = "Update a collection",
            description = "Updates an existing collection. Requires store manager permissions."
    )
    @PutMapping("/{collectionId}")
    public ResponseEntity<Collection> updateCollection(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId,
            @Parameter(description = "Collection ID", required = true)
            @PathVariable Long collectionId,
            @Valid @RequestBody CollectionDTO collectionDTO,
            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        Collection collection = collectionService.updateCollection(collectionId, collectionDTO, currentUser);
        return ResponseEntity.ok(collection);
    }

    @Operation(
            summary = "Delete a collection",
            description = "Deletes a collection. Requires store manager permissions."
    )
    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId,
            @Parameter(description = "Collection ID", required = true)
            @PathVariable Long collectionId,
            Authentication authentication) {
        Users currentUser = userService.getUserByUsername(authentication.getName());
        collectionService.deleteCollection(collectionId, currentUser);
        return ResponseEntity.noContent().build();
    }
}