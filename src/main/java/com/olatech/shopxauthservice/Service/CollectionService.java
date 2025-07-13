package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.DTO.CollectionDTO;
import com.olatech.shopxauthservice.Model.*;
import com.olatech.shopxauthservice.Repository.CollectionRepository;
import com.olatech.shopxauthservice.Repository.ProductRepository;
import com.olatech.shopxauthservice.Repository.StoreRoleRepository;
import com.olatech.shopxauthservice.exceptions.ResourceNotFoundException;
import com.olatech.shopxauthservice.exceptions.UnauthorizedException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CollectionService {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRoleRepository storeRoleRepository;

    @Transactional
    public Collection createCollection(CollectionDTO collectionDTO, Users currentUser) {
        Store store = storeService.getStoreById(collectionDTO.getStoreId(), currentUser);

        if (!hasManagementAccess(store, currentUser)) {
            throw new UnauthorizedException("No permission to create collection");
        }

        Collection collection = new Collection();
        collection.setName(collectionDTO.getName());
        collection.setDescription(collectionDTO.getDescription());
        collection.setStore(store);
        collection.setCreatedAt(LocalDateTime.now());
        collection.setUpdatedAt(LocalDateTime.now());

        if (collectionDTO.getProductIds() != null && !collectionDTO.getProductIds().isEmpty()) {
            Set<Product> products = collectionDTO.getProductIds().stream()
                    .map(id -> productRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id)))
                    .collect(Collectors.toSet());
            collection.setProducts(products);
        }

        return collectionRepository.save(collection);
    }

    public Collection getCollectionById(Long collectionId, Users currentUser) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        if (!hasManagementAccess(collection.getStore(), currentUser)) {
            throw new UnauthorizedException("No permission to view collection");
        }

        return collection;
    }

    public List<Collection> getStoreCollections(Long storeId, Users currentUser) {
        Store store = storeService.getStoreById(storeId, currentUser);
        return collectionRepository.findByStore(store);
    }

    @Transactional
    public Collection updateCollection(Long collectionId, CollectionDTO collectionDTO, Users currentUser) {
        Collection collection = getCollectionById(collectionId, currentUser);

        if (!hasManagementAccess(collection.getStore(), currentUser)) {
            throw new UnauthorizedException("No permission to update collection");
        }

        collection.setName(collectionDTO.getName());
        collection.setDescription(collectionDTO.getDescription());
        collection.setUpdatedAt(LocalDateTime.now());

        System.out.println("CollectionDTO.getProductIds() = " + collectionDTO.getProductIds());

        if (collectionDTO.getProductIds() != null) {
            Set<Product> products = collectionDTO.getProductIds().stream()
                    .map(id -> productRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id)))
                    .collect(Collectors.toSet());
            collection.setProducts(products);
        }

        return collectionRepository.save(collection);
    }

    public void deleteCollection(Long collectionId, Users currentUser) {
        Collection collection = getCollectionById(collectionId, currentUser);

        if (!hasManagementAccess(collection.getStore(), currentUser)) {
            throw new UnauthorizedException("No permission to delete collection");
        }

        collectionRepository.delete(collection);
    }

    private boolean hasManagementAccess(Store store, Users user) {
        StoreRole userRole = storeRoleRepository.findByStoreAndUser(store, user)
                .orElse(null);

        if (userRole == null) return false;

        return userRole.getRole() == StoreRole.StoreRoleType.OWNER ||
                userRole.getRole() == StoreRole.StoreRoleType.ADMIN;
    }
}