package com.olatech.shopxauthservice.Mapper;

import com.olatech.shopxauthservice.DTO.StoreVisitorDTO;
import com.olatech.shopxauthservice.Model.FrontStore.StoreVisitor;
import com.olatech.shopxauthservice.Model.Store;
import com.olatech.shopxauthservice.Repository.StoreRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper for StoreVisitor entity and DTO conversion
 */
@Mapper(componentModel = "spring")
public abstract class StoreVisitorMapper {

    @Autowired
    protected StoreRepository storeRepository;

    /**
     * Convert a StoreVisitor entity to a DTO
     *
     * @param entity The StoreVisitor entity
     * @return The StoreVisitorDTO
     */
    @Mapping(source = "store.id", target = "storeId")
    public abstract StoreVisitorDTO toDto(StoreVisitor entity);

    /**
     * Convert a StoreVisitorDTO to a StoreVisitor entity
     *
     * @param dto The StoreVisitorDTO
     * @return The StoreVisitor entity
     */
    @Mapping(source = "storeId", target = "store", qualifiedByName = "storeIdToStore")
    public abstract StoreVisitor toEntity(StoreVisitorDTO dto);

    /**
     * Update a StoreVisitor entity from a DTO
     *
     * @param dto The StoreVisitorDTO with updated values
     * @param entity The StoreVisitor entity to update
     */
    @Mapping(source = "storeId", target = "store", qualifiedByName = "storeIdToStore")
    public abstract void updateEntityFromDto(StoreVisitorDTO dto, @MappingTarget StoreVisitor entity);

    /**
     * Convert a store ID to a Store entity
     *
     * @param storeId The store ID
     * @return The Store entity
     */
    @Named("storeIdToStore")
    protected Store storeIdToStore(Long storeId) {
        if (storeId == null) {
            return null;
        }
        return storeRepository.findById(storeId).orElse(null);
    }
}
