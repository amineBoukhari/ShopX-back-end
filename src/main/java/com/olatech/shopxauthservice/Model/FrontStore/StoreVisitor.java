package com.olatech.shopxauthservice.Model.FrontStore;

import com.olatech.shopxauthservice.Model.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_visitors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreVisitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @ManyToOne // Many visitors to one store
    @JoinColumn(name = "store_id")
    private Store store;

    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private String email;

    private String phone;

    private String discoverySource;

    private Boolean newsletter;

    private String source;

    private String userAgent;

    private String ipAddress;

    private String lastPageVisited;

    private Integer totalVisits;

    private Integer totalPageViews;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}