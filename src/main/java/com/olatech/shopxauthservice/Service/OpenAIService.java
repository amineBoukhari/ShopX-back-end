package com.olatech.shopxauthservice.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Service
public class OpenAIService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public OpenAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> generateWebsiteTheme(String themePrompt) {
        try {
            logger.info("🤖 Starting OpenAI theme generation for prompt: '{}'", themePrompt);
            
            // Validate API key
            if (apiKey == null || apiKey.trim().isEmpty() || !apiKey.startsWith("sk-")) {
                logger.error("❌ Invalid OpenAI API key format");
                return getFallbackTheme(themePrompt);
            }

            // Prepare OpenAI request with improved prompt
            Map<String, Object> requestBody = createEnhancedOpenAIRequest(themePrompt);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            logger.info("📡 Making OpenAI API call...");
            
            // Make API call with timeout handling
            ResponseEntity<String> response = restTemplate.exchange(
                "https://api.openai.com/v1/chat/completions",
                HttpMethod.POST,
                entity,
                String.class
            );

            logger.info("✅ OpenAI API response status: {}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> parsedResponse = parseAndValidateOpenAIResponse(response.getBody());
                logger.info("🎨 Successfully generated AI theme with {} color variables", 
                    ((Map<String, String>) parsedResponse.get("colors")).size());
                return parsedResponse;
            } else {
                logger.error("❌ OpenAI API call failed with status: {}", response.getStatusCode());
                logger.error("Response body: {}", response.getBody());
                return getFallbackTheme(themePrompt);
            }

        } catch (Exception e) {
            logger.error("❌ Exception in OpenAI API call: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            logger.info("🔄 Using fallback theme due to exception");
            return getFallbackTheme(themePrompt);
        }
    }

    private Map<String, Object> createEnhancedOpenAIRequest(String themePrompt) {
        String enhancedSystemPrompt = """
        You are a professional web designer AI. Create a complete website theme based on the user's description.

        **CRITICAL INSTRUCTIONS:**
        1. Return ONLY valid JSON - no markdown, no explanations, no extra text
        2. Use the EXACT structure shown below
        3. All colors must be valid hex codes (#RRGGBB format)
        4. Ensure high contrast ratios for accessibility
        5. Create harmonious color palettes using color theory

        **REQUIRED JSON STRUCTURE (copy this structure exactly):**
        {
          "templateId": "descriptive-kebab-case-id",
          "title": "Website Title",
          "description": "SEO meta description 120-160 characters",
          "colors": {
            "primary": "#3B82F6",
            "secondary": "#6B7280", 
            "accent": "#10B981",
            "background": "#FFFFFF",
            "surface": "#F8FAFC",
            "text": "#1E293B",
            "textSecondary": "#64748B",
            "border": "#E2E8F0",
            "success": "#22C55E",
            "warning": "#F59E0B",
            "error": "#EF4444",
            "info": "#3B82F6"
          },
          "typography": {
            "fontFamily": "'Inter', system-ui, sans-serif",
            "headingWeight": "700",
            "bodyWeight": "400", 
            "lineHeight": "1.5"
          },
          "design": {
            "style": "modern",
            "mood": "professional",
            "borderRadius": "8px",
            "spacing": "comfortable",
            "shadows": "subtle"
          },
          "content": {
            "heroHeadline": "Compelling Main Headline",
            "heroSubtitle": "Supporting subtitle that explains the value proposition clearly",
            "ctaButton": "Get Started",
            "aboutTitle": "About Us",
            "aboutText": "Brief description of the company and its mission statement",
            "footerText": "© 2024 Company Name. All rights reserved.",
            "features": [
              "Key benefit number one",
              "Important feature two",
              "Third compelling advantage"
            ]
          },
          "layout": {
            "header": {
              "style": "split",
              "sticky": true,
              "showLogo": true
            },
            "hero": {
              "layout": "centered",
              "height": "large"
            },
            "sections": [
              {"type": "features", "enabled": true, "columns": 3},
              {"type": "about", "enabled": true},
              {"type": "contact", "enabled": true}
            ]
          },
          "seo": {
            "keywords": ["keyword1", "keyword2", "keyword3"],
            "focusKeyword": "main-keyword",
            "language": "en"
          }
        }

        **COLOR PALETTE RULES:**
        - Primary: Main brand color (vibrant, represents the brand)
        - Secondary: Supporting color (neutral, complements primary)
        - Accent: Call-to-action color (high contrast, draws attention)
        - Background: Page background (usually white or very light)
        - Surface: Card/section backgrounds (slightly darker than background)
        - Text: Main text color (dark, high contrast with background)
        - TextSecondary: Secondary text (lighter than main text)
        - Border: Subtle borders and dividers (very light)

        **DESIGN OPTIONS:**
        - style: "modern" | "minimal" | "classic" | "bold" | "creative"
        - mood: "professional" | "friendly" | "elegant" | "playful" | "trustworthy"
        - borderRadius: "0px" | "4px" | "8px" | "12px" | "16px"
        - spacing: "compact" | "comfortable" | "spacious"
        - shadows: "none" | "subtle" | "medium" | "strong"

        **CONTENT GUIDELINES:**
        - heroHeadline: 40-60 characters, action-oriented
        - heroSubtitle: 80-120 characters, explains value
        - ctaButton: 1-3 words, action verb
        - aboutText: 150-200 characters, company description
        - features: 3 items, each 30-50 characters

        Generate a theme that matches the user's request while following these rules exactly.
        """;

        Map<String, Object> systemMessage = Map.of(
            "role", "system",
            "content", enhancedSystemPrompt
        );

        // Enhanced user message with more context
        String enhancedUserPrompt = String.format(
            "Create a website theme for: %s\n\n" +
            "Please generate a complete theme with:\n" +
            "- A harmonious color palette that reflects the described style\n" +
            "- Appropriate typography for the target audience\n" +
            "- Content that matches the business/purpose\n" +
            "- Professional design choices\n\n" +
            "Return only the JSON structure as specified.",
            themePrompt
        );

        Map<String, Object> userMessage = Map.of(
            "role", "user", 
            "content", enhancedUserPrompt
        );

        return Map.of(
            "model", "gpt-3.5-turbo",
            "messages", List.of(systemMessage, userMessage),
            "temperature", 0.8,  // Slightly higher for more creativity
            "max_tokens", 1200,  // Increased for complete responses
            "top_p", 0.9,       // Focus on high probability tokens
            "frequency_penalty", 0.0,
            "presence_penalty", 0.0
        );
    }

    private Map<String, Object> parseAndValidateOpenAIResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // Check for API errors
            if (jsonNode.has("error")) {
                logger.error("❌ OpenAI API error: {}", jsonNode.get("error").asText());
                return getFallbackTheme("API Error");
            }
            
            String generatedContent = jsonNode.path("choices").get(0).path("message").path("content").asText();
            
            logger.info("🤖 Raw OpenAI response: {}", generatedContent);
            
            // Clean the response (remove markdown if present)
            String cleanedContent = cleanJsonResponse(generatedContent);
            
            // Parse the generated JSON content
            Map<String, Object> themeData = objectMapper.readValue(cleanedContent, Map.class);
            
            // Validate and fix the response structure
            Map<String, Object> validatedTheme = validateAndFixThemeStructure(themeData);
            
            logger.info("✅ Successfully parsed and validated AI theme");
            return validatedTheme;
            
        } catch (JsonProcessingException e) {
            logger.error("❌ Error parsing OpenAI JSON response: {}", e.getMessage());
            logger.info("📄 Raw response that failed: {}", responseBody);
            return getFallbackTheme("JSON Parse Error");
        } catch (Exception e) {
            logger.error("❌ Unexpected error parsing response: {}", e.getMessage());
            return getFallbackTheme("Parse Error");
        }
    }

    private String cleanJsonResponse(String response) {
        // Remove markdown code blocks if present
        response = response.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "");
        
        // Remove any leading/trailing whitespace
        response = response.trim();
        
        // Find the first { and last } to extract just the JSON
        int firstBrace = response.indexOf('{');
        int lastBrace = response.lastIndexOf('}');
        
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            response = response.substring(firstBrace, lastBrace + 1);
        }
        
        return response;
    }

    private Map<String, Object> validateAndFixThemeStructure(Map<String, Object> themeData) {
        Map<String, Object> validatedTheme = new HashMap<>();
        
        // Basic info with fallbacks
        validatedTheme.put("templateId", getStringValue(themeData, "templateId", "ai-generated-theme"));
        validatedTheme.put("title", getStringValue(themeData, "title", "Mon Site Web"));
        validatedTheme.put("description", getStringValue(themeData, "description", "Site web généré par IA"));
        
        // Colors - ensure all required colors are present
        Map<String, String> colors = validateColors((Map<String, Object>) themeData.get("colors"));
        validatedTheme.put("colors", colors);
        
        // Typography - ensure all required typography settings
        Map<String, String> typography = validateTypography((Map<String, Object>) themeData.get("typography"));
        validatedTheme.put("typography", typography);
        
        // Design - ensure all required design settings  
        Map<String, String> design = validateDesign((Map<String, Object>) themeData.get("design"));
        validatedTheme.put("design", design);
        
        // Content - ensure all required content
        Map<String, Object> content = validateContent((Map<String, Object>) themeData.get("content"));
        validatedTheme.put("content", content);
        
        // Layout - ensure layout structure
        Map<String, Object> layout = validateLayout((Map<String, Object>) themeData.get("layout"));
        validatedTheme.put("layout", layout);
        
        // SEO
        Map<String, Object> seo = validateSEO((Map<String, Object>) themeData.get("seo"));
        validatedTheme.put("seo", seo);
        
        return validatedTheme;
    }

    private Map<String, String> validateColors(Map<String, Object> colors) {
        Map<String, String> validatedColors = new HashMap<>();
        
        // Required colors with fallbacks
        validatedColors.put("primary", getColorValue(colors, "primary", "#3B82F6"));
        validatedColors.put("secondary", getColorValue(colors, "secondary", "#6B7280"));
        validatedColors.put("accent", getColorValue(colors, "accent", "#10B981"));
        validatedColors.put("background", getColorValue(colors, "background", "#FFFFFF"));
        validatedColors.put("surface", getColorValue(colors, "surface", "#F8FAFC"));
        validatedColors.put("text", getColorValue(colors, "text", "#1E293B"));
        validatedColors.put("textSecondary", getColorValue(colors, "textSecondary", "#64748B"));
        validatedColors.put("border", getColorValue(colors, "border", "#E2E8F0"));
        validatedColors.put("success", getColorValue(colors, "success", "#22C55E"));
        validatedColors.put("warning", getColorValue(colors, "warning", "#F59E0B"));
        validatedColors.put("error", getColorValue(colors, "error", "#EF4444"));
        validatedColors.put("info", getColorValue(colors, "info", "#3B82F6"));
        
        return validatedColors;
    }

    private Map<String, String> validateTypography(Map<String, Object> typography) {
        Map<String, String> validatedTypography = new HashMap<>();
        
        validatedTypography.put("fontFamily", getStringValue(typography, "fontFamily", "'Inter', system-ui, sans-serif"));
        validatedTypography.put("headingWeight", getStringValue(typography, "headingWeight", "700"));
        validatedTypography.put("bodyWeight", getStringValue(typography, "bodyWeight", "400"));
        validatedTypography.put("lineHeight", getStringValue(typography, "lineHeight", "1.5"));
        
        return validatedTypography;
    }

    private Map<String, String> validateDesign(Map<String, Object> design) {
        Map<String, String> validatedDesign = new HashMap<>();
        
        validatedDesign.put("style", getStringValue(design, "style", "modern"));
        validatedDesign.put("mood", getStringValue(design, "mood", "professional"));
        validatedDesign.put("borderRadius", getStringValue(design, "borderRadius", "8px"));
        validatedDesign.put("spacing", getStringValue(design, "spacing", "comfortable"));
        validatedDesign.put("shadows", getStringValue(design, "shadows", "subtle"));
        
        return validatedDesign;
    }

    private Map<String, Object> validateContent(Map<String, Object> content) {
        Map<String, Object> validatedContent = new HashMap<>();
        
        validatedContent.put("heroHeadline", getStringValue(content, "heroHeadline", "Bienvenue sur notre site"));
        validatedContent.put("heroSubtitle", getStringValue(content, "heroSubtitle", "Découvrez nos services exceptionnels"));
        validatedContent.put("ctaButton", getStringValue(content, "ctaButton", "Découvrir"));
        validatedContent.put("aboutTitle", getStringValue(content, "aboutTitle", "À Propos"));
        validatedContent.put("aboutText", getStringValue(content, "aboutText", "Nous sommes une entreprise innovante"));
        validatedContent.put("footerText", getStringValue(content, "footerText", "© 2024 Mon Site. Tous droits réservés."));
        
        // Features array
        List<String> features = (List<String>) content.get("features");
        if (features == null || features.isEmpty()) {
            features = Arrays.asList(
                "Service de qualité",
                "Équipe professionnelle", 
                "Support client 24/7"
            );
        }
        validatedContent.put("features", features);
        
        return validatedContent;
    }

    private Map<String, Object> validateLayout(Map<String, Object> layout) {
        Map<String, Object> validatedLayout = new HashMap<>();
        
        // Header
        Map<String, Object> header = new HashMap<>();
        Map<String, Object> layoutHeader = (Map<String, Object>) (layout != null ? layout.get("header") : null);
        header.put("style", getStringValue(layoutHeader, "style", "split"));
        header.put("sticky", getBooleanValue(layoutHeader, "sticky", true));
        header.put("showLogo", getBooleanValue(layoutHeader, "showLogo", true));
        validatedLayout.put("header", header);
        
        // Hero
        Map<String, Object> hero = new HashMap<>();
        Map<String, Object> layoutHero = (Map<String, Object>) (layout != null ? layout.get("hero") : null);
        hero.put("layout", getStringValue(layoutHero, "layout", "centered"));
        hero.put("height", getStringValue(layoutHero, "height", "large"));
        validatedLayout.put("hero", hero);
        
        return validatedLayout;
    }

    private Map<String, Object> validateSEO(Map<String, Object> seo) {
        Map<String, Object> validatedSEO = new HashMap<>();
        
        List<String> keywords = (List<String>) (seo != null ? seo.get("keywords") : null);
        if (keywords == null || keywords.isEmpty()) {
            keywords = Arrays.asList("site web", "entreprise", "services");
        }
        validatedSEO.put("keywords", keywords);
        validatedSEO.put("focusKeyword", getStringValue(seo, "focusKeyword", "site web"));
        validatedSEO.put("language", getStringValue(seo, "language", "fr"));
        
        return validatedSEO;
    }

    // Helper methods
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return defaultValue;
        }
        return String.valueOf(map.get(key));
    }

    private boolean getBooleanValue(Map<String, Object> map, String key, boolean defaultValue) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private String getColorValue(Map<String, Object> colors, String key, String defaultValue) {
        if (colors == null || !colors.containsKey(key) || colors.get(key) == null) {
            return defaultValue;
        }
        String color = String.valueOf(colors.get(key));
        // Validate hex color format
        if (color.matches("^#[0-9A-Fa-f]{6}$")) {
            return color;
        }
        logger.warn("⚠️ Invalid color format for {}: {}, using default", key, color);
        return defaultValue;
    }

    private Map<String, Object> getFallbackTheme(String prompt) {
        logger.info("🔄 Using enhanced fallback theme for prompt: {}", prompt);
        
        Map<String, Object> fallbackTheme = new HashMap<>();
        
        // Basic info
        fallbackTheme.put("templateId", "fallback-theme-" + System.currentTimeMillis());
        fallbackTheme.put("title", "Mon Site Web");
        fallbackTheme.put("description", "Site web professionnel avec un design moderne et élégant");
        
        // Complete colors matching ThemeFileService expectations
        Map<String, String> colors = new HashMap<>();
        colors.put("primary", "#3B82F6");
        colors.put("secondary", "#6B7280");
        colors.put("accent", "#10B981");
        colors.put("background", "#FFFFFF");
        colors.put("surface", "#F8FAFC");
        colors.put("text", "#1E293B");
        colors.put("textSecondary", "#64748B");
        colors.put("border", "#E2E8F0");
        colors.put("success", "#22C55E");
        colors.put("warning", "#F59E0B");
        colors.put("error", "#EF4444");
        colors.put("info", "#3B82F6");
        fallbackTheme.put("colors", colors);

        // Typography
        Map<String, String> typography = new HashMap<>();
        typography.put("fontFamily", "'Inter', system-ui, sans-serif");
        typography.put("headingWeight", "700");
        typography.put("bodyWeight", "400");
        typography.put("lineHeight", "1.5");
        fallbackTheme.put("typography", typography);

        // Design
        Map<String, String> design = new HashMap<>();
        design.put("style", "modern");
        design.put("mood", "professional");
        design.put("borderRadius", "8px");
        design.put("spacing", "comfortable");
        design.put("shadows", "subtle");
        fallbackTheme.put("design", design);

        // Content
        Map<String, Object> content = new HashMap<>();
        content.put("heroHeadline", "Bienvenue sur notre site");
        content.put("heroSubtitle", "Découvrez nos services exceptionnels et notre expertise");
        content.put("ctaButton", "Découvrir");
        content.put("aboutTitle", "À Propos de Nous");
        content.put("aboutText", "Nous sommes une entreprise innovante dédiée à l'excellence");
        content.put("footerText", "© 2024 Mon Site. Tous droits réservés.");
        content.put("features", Arrays.asList(
            "Service de qualité premium",
            "Équipe professionnelle expérimentée",
            "Support client disponible 24/7"
        ));
        fallbackTheme.put("content", content);

        // Layout
        Map<String, Object> layout = new HashMap<>();
        Map<String, Object> header = new HashMap<>();
        header.put("style", "split");
        header.put("sticky", true);
        header.put("showLogo", true);
        layout.put("header", header);
        
        Map<String, Object> hero = new HashMap<>();
        hero.put("layout", "centered");
        hero.put("height", "large");
        layout.put("hero", hero);
        fallbackTheme.put("layout", layout);

        // SEO
        Map<String, Object> seo = new HashMap<>();
        seo.put("keywords", Arrays.asList("site web", "entreprise", "services", "professionnel"));
        seo.put("focusKeyword", "site web");
        seo.put("language", "fr");
        fallbackTheme.put("seo", seo);

        return fallbackTheme;
    }
}