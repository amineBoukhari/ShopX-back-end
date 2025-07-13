package com.olatech.shopxauthservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Store Visitor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreVisitorDTO {
    private Long id;
    private String userId;
    private Long storeId;
    private String firstName;
    private String lastName;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
