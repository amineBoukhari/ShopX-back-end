package com.olatech.shopxauthservice.Model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;



public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "currency_sequence", sequenceName = "currency_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 3)
    private String code;  // ISO 4217 currency code (e.g., USD, EUR, XOF)

    @Column(nullable = false)
    private String name;  // Full name of the currency

    private String symbol;  // Currency symbol (e.g., $, €, CFA)

    @Column(precision = 10, scale = 4)
    private BigDecimal exchangeRate;  // Exchange rate relative to base currency

    private boolean isBaseCurrency = false;
    private boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    private LocalDateTime lastUpdated;
}