package com.olatech.shopxauthservice.DTO.analysis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductAnalysisResponse {
    private int overallScore;
    private List<SuggestionDto> suggestions;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SuggestionDto {
        private String field;
        private String currentValue;

        // Changé de String à Object pour accepter soit une chaîne soit un tableau
        private Object suggestedValue;

        private String justification;
        private int scoreImprovement;

        // Getter qui convertit l'objet en String quelle que soit sa nature
        public String getSuggestedValueAsString() {
            if (suggestedValue == null) {
                return "";
            }

            // Si c'est déjà une chaîne, la retourner directement
            if (suggestedValue instanceof String) {
                return (String) suggestedValue;
            }

            // Si c'est un tableau, le convertir en liste formatée
            if (suggestedValue instanceof List) {
                StringBuilder sb = new StringBuilder();
                List<?> list = (List<?>) suggestedValue;

                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) sb.append("\n");
                    sb.append("• ").append(list.get(i));
                }

                return sb.toString();
            }

            // Pour tout autre type, utiliser toString()
            return suggestedValue.toString();
        }
    }
}