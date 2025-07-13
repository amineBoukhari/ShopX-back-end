package com.olatech.shopxauthservice.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class StoreRole {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id")
    @JsonBackReference
    private Store store;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Enumerated(EnumType.STRING)
    private StoreRoleType role;

    public void setUserId(int userId) {
        this.user = new Users();
        this.user.setId(userId);
    }

    public enum StoreRoleType {
        OWNER,
        ADMIN,
        MANAGER,
        STAFF
    }

    // Getters and Setters

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public StoreRoleType getRole() {
        return role;
    }

    public void setRole(StoreRoleType role) {
        this.role = role;
    }
}