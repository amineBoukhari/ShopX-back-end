package com.olatech.shopxauthservice.Model.analysis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProductSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "suggestion_sequence", sequenceName = "suggestion_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "suggestion_set_id", nullable = false)
    @JsonIgnore
    private ProductSuggestionSet suggestionSet;

    private String field;
    @Column(columnDefinition = "TEXT")
    private String currentValue;
    @Column(columnDefinition = "TEXT")
    private String suggestedValue;
    @Column(columnDefinition = "TEXT")
    private String justification;
    private Integer scoreImprovement;

    @Enumerated(EnumType.STRING)
    private SuggestionStatus status = SuggestionStatus.PENDING;

    public enum SuggestionStatus {
        PENDING,
        ACCEPTED,
        MODIFIED,
        REJECTED
    }
}