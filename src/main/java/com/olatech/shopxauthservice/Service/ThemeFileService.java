package com.olatech.shopxauthservice.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class ThemeFileService {

    private static final Logger logger = LoggerFactory.getLogger(ThemeFileService.class);
    private final String THEMES_DIR = "src/main/resources/static/themes/";

    public void generateThemeFiles(String subdomain, Map<String, Object> aiTheme) {
        try {
            Path themeDir = Paths.get(THEMES_DIR + subdomain);
            Files.createDirectories(themeDir);

            generateCSSFile(themeDir, aiTheme);
            generateConfigFile(themeDir, aiTheme);
            generateUtilitiesCSS(themeDir, aiTheme);

            logger.info("Enhanced theme files generated for subdomain: {}", subdomain);
        } catch (IOException e) {
            logger.error("Error generating theme files for {}: {}", subdomain, e.getMessage());
            throw new RuntimeException("Failed to generate theme files", e);
        }
    }

private void generateCSSFile(Path themeDir, Map<String, Object> aiTheme) throws IOException {
    StringBuilder css = new StringBuilder();
    
    // Extract theme sections
    Map<String, String> colors = (Map<String, String>) aiTheme.get("colors");
    Map<String, String> typography = (Map<String, String>) aiTheme.get("typography");
    Map<String, String> design = (Map<String, String>) aiTheme.get("design");
    Map<String, Object> layout = (Map<String, Object>) aiTheme.get("layout");

    css.append("/* AI Generated Enhanced Theme CSS */\n");
    css.append("/* Generated at: ").append(java.time.LocalDateTime.now()).append(" */\n\n");

    // CSS Custom Properties (CSS Variables) - ALL VARIABLES MUST BE SET
    css.append(":root {\n");
    css.append("  /* ===============================\n");
    css.append("     COMPLETE COLOR PALETTE\n");
    css.append("     =============================== */\n");
    
    // Core colors (with fallbacks if not provided by AI)
    css.append("  --theme-primary: ").append(getColorValue(colors, "primary", "#3B82F6")).append(";\n");
    css.append("  --theme-secondary: ").append(getColorValue(colors, "secondary", "#6B7280")).append(";\n");
    css.append("  --theme-accent: ").append(getColorValue(colors, "accent", "#10B981")).append(";\n");
    css.append("  --theme-background: ").append(getColorValue(colors, "background", "#FFFFFF")).append(";\n");
    css.append("\n");
    
    // Text colors (REQUIRED - must always be set)
    css.append("  /* Text Colors */\n");
    css.append("  --theme-text: ").append(getColorValue(colors, "text", "#1e293b")).append(";\n");
    css.append("  --theme-textSecondary: ").append(getColorValue(colors, "textSecondary", "#64748b")).append(";\n");
    css.append("\n");
    
    // Surface colors (REQUIRED - must always be set)
    css.append("  /* Surface Colors */\n");
    css.append("  --theme-surface: ").append(getColorValue(colors, "surface", "#f8fafc")).append(";\n");
    css.append("  --theme-border: ").append(getColorValue(colors, "border", "#e2e8f0")).append(";\n");
    css.append("\n");
    
    // Status colors (REQUIRED - must always be set)
    css.append("  /* Status Colors */\n");
    css.append("  --theme-success: ").append(getColorValue(colors, "success", "#22c55e")).append(";\n");
    css.append("  --theme-warning: ").append(getColorValue(colors, "warning", "#f59e0b")).append(";\n");
    css.append("  --theme-error: ").append(getColorValue(colors, "error", "#ef4444")).append(";\n");
    css.append("  --theme-info: ").append(getColorValue(colors, "info", "#3b82f6")).append(";\n");
    css.append("\n");

    // Typography (REQUIRED - must always be set)
    css.append("  /* ===============================\n");
    css.append("     TYPOGRAPHY SYSTEM\n");
    css.append("     =============================== */\n");
    css.append("  --theme-font-family: ").append(getTypographyValue(typography, "fontFamily", "'Inter', system-ui, sans-serif")).append(";\n");
    css.append("  --theme-heading-weight: ").append(getTypographyValue(typography, "headingWeight", "700")).append(";\n");
    css.append("  --theme-body-weight: ").append(getTypographyValue(typography, "bodyWeight", "400")).append(";\n");
    css.append("  --theme-line-height: ").append(getTypographyValue(typography, "lineHeight", "1.5")).append(";\n");
    css.append("\n");

    // Spacing & Layout (REQUIRED - must always be set)
    css.append("  /* ===============================\n");
    css.append("     SPACING & LAYOUT\n");
    css.append("     =============================== */\n");
    css.append("  --theme-spacing: ").append(getSpacingValue(design)).append(";\n");
    css.append("  --theme-radius: ").append(getRadiusValue(design)).append(";\n");
    css.append("  --theme-shadow: ").append(getShadowValue(design)).append(";\n");
    css.append("\n");

    css.append("}\n\n");

    // Dark Mode Support (REQUIRED)
    css.append("/* Dark Mode Variables */\n");
    css.append("@media (prefers-color-scheme: dark) {\n");
    css.append("  :root {\n");
    css.append("    --theme-background: ").append(getDarkModeColor("background", "#0f172a")).append(";\n");
    css.append("    --theme-text: ").append(getDarkModeColor("text", "#f1f5f9")).append(";\n");
    css.append("    --theme-textSecondary: ").append(getDarkModeColor("textSecondary", "#94a3b8")).append(";\n");
    css.append("    --theme-surface: ").append(getDarkModeColor("surface", "#1e293b")).append(";\n");
    css.append("    --theme-border: ").append(getDarkModeColor("border", "#334155")).append(";\n");
    css.append("    --theme-shadow: ").append(getDarkModeColor("shadow", "0 2px 8px rgba(0, 0, 0, 0.3)")).append(";\n");
    css.append("  }\n");
    css.append("}\n\n");

    // Base styles
    css.append("/* ===============================\n");
    css.append("   BASE STYLES\n");
    css.append("   =============================== */\n");
    css.append("* {\n");
    css.append("  box-sizing: border-box;\n");
    css.append("  margin: 0;\n");
    css.append("  padding: 0;\n");
    css.append("}\n\n");

    css.append("body {\n");
    css.append("  font-family: var(--theme-font-family);\n");
    css.append("  font-weight: var(--theme-body-weight);\n");
    css.append("  line-height: var(--theme-line-height);\n");
    css.append("  background-color: var(--theme-background);\n");
    css.append("  color: var(--theme-text);\n");
    css.append("  -webkit-font-smoothing: antialiased;\n");
    css.append("  -moz-osx-font-smoothing: grayscale;\n");
    css.append("}\n\n");

    // Typography styles
    css.append("/* ===============================\n");
    css.append("   TYPOGRAPHY\n");
    css.append("   =============================== */\n");
    css.append("h1, h2, h3, h4, h5, h6 {\n");
    css.append("  font-weight: var(--theme-heading-weight);\n");
    css.append("  line-height: 1.2;\n");
    css.append("  margin-bottom: var(--theme-spacing);\n");
    css.append("  color: var(--theme-text);\n");
    css.append("}\n\n");

    css.append("h1 { font-size: 3rem; }\n");
    css.append("h2 { font-size: 2.25rem; }\n");
    css.append("h3 { font-size: 1.875rem; }\n");
    css.append("h4 { font-size: 1.5rem; }\n");
    css.append("h5 { font-size: 1.25rem; }\n");
    css.append("h6 { font-size: 1.125rem; }\n\n");

    css.append("p {\n");
    css.append("  margin-bottom: var(--theme-spacing);\n");
    css.append("  line-height: var(--theme-line-height);\n");
    css.append("}\n\n");

    // Text utility classes (REQUIRED)
    css.append("/* Text Utility Classes */\n");
    css.append(".text-primary { color: var(--theme-primary); }\n");
    css.append(".text-secondary { color: var(--theme-textSecondary); }\n");
    css.append(".text-success { color: var(--theme-success); }\n");
    css.append(".text-warning { color: var(--theme-warning); }\n");
    css.append(".text-error { color: var(--theme-error); }\n");
    css.append(".text-info { color: var(--theme-info); }\n\n");

    // Layout components
    generateLayoutCSS(css, layout);

    // Component styles (enhanced)
    generateEnhancedComponentCSS(css);

    // Utility classes (enhanced)
    generateEnhancedUtilityCSS(css);

    // Responsive design
    generateResponsiveCSS(css);

    Files.write(themeDir.resolve("theme.css"), css.toString().getBytes());
}

private String getColorValue(Map<String, String> colors, String key, String defaultValue) {
    if (colors != null && colors.containsKey(key) && colors.get(key) != null) {
        return colors.get(key);
    }
    return defaultValue;
}

private String getTypographyValue(Map<String, String> typography, String key, String defaultValue) {
    if (typography != null && typography.containsKey(key) && typography.get(key) != null) {
        return typography.get(key);
    }
    return defaultValue;
}

private String getSpacingValue(Map<String, String> design) {
    if (design != null && design.containsKey("spacing")) {
        String spacing = design.get("spacing");
        return switch (spacing) {
            case "compact" -> "0.75rem";
            case "comfortable" -> "1rem";
            case "spacious" -> "1.5rem";
            default -> "1rem";
        };
    }
    return "1rem"; // Default fallback
}

private String getRadiusValue(Map<String, String> design) {
    if (design != null && design.containsKey("borderRadius")) {
        String radius = design.get("borderRadius");
        return switch (radius) {
            case "none" -> "0px";
            case "small" -> "4px";
            case "medium" -> "8px";
            case "large" -> "16px";
            case "rounded" -> "9999px";
            default -> radius; // Use exact value if provided
        };
    }
    return "8px"; // Default fallback
}

private String getShadowValue(Map<String, String> design) {
    if (design != null && design.containsKey("shadows")) {
        String shadow = design.get("shadows");
        return switch (shadow) {
            case "none" -> "none";
            case "subtle" -> "0 1px 3px rgba(0, 0, 0, 0.1)";
            case "medium" -> "0 4px 12px rgba(0, 0, 0, 0.15)";
            case "strong" -> "0 8px 25px rgba(0, 0, 0, 0.25)";
            default -> "0 2px 8px rgba(0, 0, 0, 0.1)";
        };
    }
    return "0 2px 8px rgba(0, 0, 0, 0.1)"; // Default fallback
}

private String getDarkModeColor(String colorType, String defaultValue) {
    // You can extend this to read from AI theme data if dark mode colors are provided
    return defaultValue;
}

private void generateEnhancedComponentCSS(StringBuilder css) {
    css.append("/* ===============================\n");
    css.append("   ENHANCED COMPONENT STYLES\n");
    css.append("   =============================== */\n");

    // Buttons (Enhanced)
    css.append(".btn {\n");
    css.append("  display: inline-block;\n");
    css.append("  padding: 0.75rem 1.5rem;\n");
    css.append("  font-weight: 500;\n");
    css.append("  text-decoration: none;\n");
    css.append("  border: none;\n");
    css.append("  border-radius: var(--theme-radius);\n");
    css.append("  cursor: pointer;\n");
    css.append("  transition: all 0.2s ease;\n");
    css.append("  text-align: center;\n");
    css.append("  font-size: 0.875rem;\n");
    css.append("  line-height: 1.25;\n");
    css.append("  font-family: var(--theme-font-family);\n");
    css.append("}\n\n");

    css.append(".btn:focus {\n");
    css.append("  outline: 2px solid var(--theme-primary);\n");
    css.append("  outline-offset: 2px;\n");
    css.append("}\n\n");

    css.append(".btn:disabled {\n");
    css.append("  opacity: 0.6;\n");
    css.append("  cursor: not-allowed;\n");
    css.append("  transform: none;\n");
    css.append("}\n\n");

    css.append(".btn-primary {\n");
    css.append("  background-color: var(--theme-primary);\n");
    css.append("  color: white;\n");
    css.append("  box-shadow: var(--theme-shadow);\n");
    css.append("}\n\n");

    css.append(".btn-primary:hover:not(:disabled) {\n");
    css.append("  transform: translateY(-1px);\n");
    css.append("  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.25);\n");
    css.append("}\n\n");

    css.append(".btn-secondary {\n");
    css.append("  background-color: var(--theme-surface);\n");
    css.append("  color: var(--theme-text);\n");
    css.append("  border: 1px solid var(--theme-border);\n");
    css.append("}\n\n");

    css.append(".btn-secondary:hover:not(:disabled) {\n");
    css.append("  background-color: var(--theme-border);\n");
    css.append("}\n\n");

    css.append(".btn-accent {\n");
    css.append("  background-color: var(--theme-accent);\n");
    css.append("  color: white;\n");
    css.append("  box-shadow: var(--theme-shadow);\n");
    css.append("}\n\n");

    css.append(".btn-accent:hover:not(:disabled) {\n");
    css.append("  transform: translateY(-1px);\n");
    css.append("  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25);\n");
    css.append("}\n\n");

    // Cards (Enhanced)
    css.append(".card {\n");
    css.append("  background-color: var(--theme-surface);\n");
    css.append("  border: 1px solid var(--theme-border);\n");
    css.append("  border-radius: var(--theme-radius);\n");
    css.append("  padding: var(--theme-spacing);\n");
    css.append("  box-shadow: var(--theme-shadow);\n");
    css.append("  transition: box-shadow 0.2s ease;\n");
    css.append("}\n\n");

    css.append(".card:hover {\n");
    css.append("  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);\n");
    css.append("}\n\n");

    // Grids (Enhanced)
    css.append(".features-grid {\n");
    css.append("  display: grid;\n");
    css.append("  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));\n");
    css.append("  gap: calc(var(--theme-spacing) * 1.5);\n");
    css.append("  margin: 3rem 0;\n");
    css.append("}\n\n");

    css.append(".products-grid {\n");
    css.append("  display: grid;\n");
    css.append("  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));\n");
    css.append("  gap: calc(var(--theme-spacing) * 2);\n");
    css.append("}\n\n");
}

private void generateEnhancedUtilityCSS(StringBuilder css) {
    css.append("/* ===============================\n");
    css.append("   ENHANCED UTILITY CLASSES\n");
    css.append("   =============================== */\n");

    // Text alignment
    css.append("/* Text Alignment */\n");
    css.append(".text-center { text-align: center; }\n");
    css.append(".text-left { text-align: left; }\n");
    css.append(".text-right { text-align: right; }\n\n");

    // Margins (Enhanced with more options)
    css.append("/* Margins */\n");
    css.append(".mb-1 { margin-bottom: 0.5rem; }\n");
    css.append(".mb-2 { margin-bottom: var(--theme-spacing); }\n");
    css.append(".mb-3 { margin-bottom: calc(var(--theme-spacing) * 1.5); }\n");
    css.append(".mb-4 { margin-bottom: calc(var(--theme-spacing) * 2); }\n");
    css.append(".mb-8 { margin-bottom: calc(var(--theme-spacing) * 4); }\n");
    css.append(".mb-12 { margin-bottom: calc(var(--theme-spacing) * 6); }\n\n");

    css.append(".mt-1 { margin-top: 0.5rem; }\n");
    css.append(".mt-2 { margin-top: var(--theme-spacing); }\n");
    css.append(".mt-3 { margin-top: calc(var(--theme-spacing) * 1.5); }\n");
    css.append(".mt-4 { margin-top: calc(var(--theme-spacing) * 2); }\n");
    css.append(".mt-8 { margin-top: calc(var(--theme-spacing) * 4); }\n\n");

    css.append(".ml-2 { margin-left: 0.5rem; }\n");
    css.append(".ml-4 { margin-left: var(--theme-spacing); }\n\n");

    // Padding
    css.append("/* Padding */\n");
    css.append(".py-8 { padding: calc(var(--theme-spacing) * 4) 0; }\n");
    css.append(".py-16 { padding: calc(var(--theme-spacing) * 8) 0; }\n");
    css.append(".py-24 { padding: calc(var(--theme-spacing) * 12) 0; }\n\n");

    css.append(".px-4 { padding: 0 var(--theme-spacing); }\n");
    css.append(".px-8 { padding: 0 calc(var(--theme-spacing) * 2); }\n\n");
}

private void generateResponsiveCSS(StringBuilder css) {
    css.append("/* ===============================\n");
    css.append("   RESPONSIVE DESIGN\n");
    css.append("   =============================== */\n");
    
    css.append("@media (max-width: 1024px) {\n");
    css.append("  .features-grid {\n");
    css.append("    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n");
    css.append("    gap: var(--theme-spacing);\n");
    css.append("  }\n");
    css.append("}\n\n");

    css.append("@media (max-width: 768px) {\n");
    css.append("  .container {\n");
    css.append("    padding: 0 var(--theme-spacing);\n");
    css.append("  }\n");
    css.append("  \n");
    css.append("  h1 { font-size: 2.25rem; }\n");
    css.append("  h2 { font-size: 1.875rem; }\n");
    css.append("  h3 { font-size: 1.5rem; }\n");
    css.append("  \n");
    css.append("  .hero {\n");
    css.append("    padding: calc(var(--theme-spacing) * 3) 0;\n");
    css.append("  }\n");
    css.append("  \n");
    css.append("  .features-grid {\n");
    css.append("    grid-template-columns: 1fr;\n");
    css.append("    gap: var(--theme-spacing);\n");
    css.append("  }\n");
    css.append("}\n\n");

    css.append("@media (max-width: 480px) {\n");
    css.append("  h1 { font-size: 1.875rem; }\n");
    css.append("  h2 { font-size: 1.5rem; }\n");
    css.append("  h3 { font-size: 1.25rem; }\n");
    css.append("}\n\n");

    // Accessibility
    css.append("/* ===============================\n");
    css.append("   ACCESSIBILITY\n");
    css.append("   =============================== */\n");
    css.append("@media (prefers-reduced-motion: reduce) {\n");
    css.append("  *,\n");
    css.append("  *::before,\n");
    css.append("  *::after {\n");
    css.append("    animation-duration: 0.01ms !important;\n");
    css.append("    animation-iteration-count: 1 !important;\n");
    css.append("    transition-duration: 0.01ms !important;\n");
    css.append("  }\n");
    css.append("}\n\n");

    // Loading spinner
    css.append("@keyframes spin {\n");
    css.append("  0% { transform: rotate(0deg); }\n");
    css.append("  100% { transform: rotate(360deg); }\n");
    css.append("}\n\n");

    css.append(".loading-spinner {\n");
    css.append("  width: 50px;\n");
    css.append("  height: 50px;\n");
    css.append("  border: 3px solid var(--theme-border);\n");
    css.append("  border-top: 3px solid var(--theme-primary);\n");
    css.append("  border-radius: 50%;\n");
    css.append("  animation: spin 1s linear infinite;\n");
    css.append("}\n");
}
    private void generateLayoutCSS(StringBuilder css, Map<String, Object> layout) {
        css.append("/* Layout Components */\n");
        
        // Container
        css.append(".container {\n");
        css.append("  max-width: 1200px;\n");
        css.append("  margin: 0 auto;\n");
        css.append("  padding: 0 var(--theme-spacing, 1rem);\n");
        css.append("}\n\n");

        // Header
        if (layout != null && layout.get("header") != null) {
            Map<String, Object> header = (Map<String, Object>) layout.get("header");
            css.append(".header {\n");
            css.append("  background-color: var(--theme-surface, #f8fafc);\n");
            css.append("  border-bottom: 1px solid var(--theme-border, #e2e8f0);\n");
            css.append("  padding: var(--theme-spacing, 1rem) 0;\n");
            
            if (Boolean.TRUE.equals(header.get("sticky"))) {
                css.append("  position: sticky;\n");
                css.append("  top: 0;\n");
                css.append("  z-index: 100;\n");
            }
            css.append("}\n\n");

            String headerStyle = (String) header.get("style");
            if ("centered".equals(headerStyle)) {
                css.append(".header-content {\n");
                css.append("  text-align: center;\n");
                css.append("}\n\n");
            } else if ("split".equals(headerStyle)) {
                css.append(".header-content {\n");
                css.append("  display: flex;\n");
                css.append("  justify-content: space-between;\n");
                css.append("  align-items: center;\n");
                css.append("}\n\n");
            }
        }

        // Hero section
        if (layout != null && layout.get("hero") != null) {
            Map<String, Object> hero = (Map<String, Object>) layout.get("hero");
            css.append(".hero {\n");
            css.append("  background: linear-gradient(135deg, var(--theme-primary), var(--theme-accent));\n");
            css.append("  color: white;\n");
            css.append("  padding: 4rem 0;\n");
            css.append("  text-align: center;\n");
            css.append("  position: relative;\n");
            css.append("  overflow: hidden;\n");
            
            String heroHeight = (String) hero.get("height");
            if ("viewport".equals(heroHeight)) {
                css.append("  min-height: 100vh;\n");
                css.append("  display: flex;\n");
                css.append("  align-items: center;\n");
            } else if ("large".equals(heroHeight)) {
                css.append("  padding: 6rem 0;\n");
            }
            css.append("}\n\n");

            String heroLayout = (String) hero.get("layout");
            if ("split".equals(heroLayout)) {
                css.append(".hero-content {\n");
                css.append("  display: grid;\n");
                css.append("  grid-template-columns: 1fr 1fr;\n");
                css.append("  gap: 3rem;\n");
                css.append("  align-items: center;\n");
                css.append("}\n\n");
                
                css.append("@media (max-width: 768px) {\n");
                css.append("  .hero-content {\n");
                css.append("    grid-template-columns: 1fr;\n");
                css.append("    text-align: center;\n");
                css.append("  }\n");
                css.append("}\n\n");
            }
        }
    }

    private void generateComponentCSS(StringBuilder css) {
        css.append("/* Component Styles */\n");

        // Buttons
        css.append(".btn {\n");
        css.append("  display: inline-block;\n");
        css.append("  padding: 0.75rem 1.5rem;\n");
        css.append("  font-weight: var(--theme-body-weight, 500);\n");
        css.append("  text-decoration: none;\n");
        css.append("  border: none;\n");
        css.append("  border-radius: var(--theme-radius, 8px);\n");
        css.append("  cursor: pointer;\n");
        css.append("  transition: all 0.2s ease;\n");
        css.append("  text-align: center;\n");
        css.append("}\n\n");

        css.append(".btn-primary {\n");
        css.append("  background-color: var(--theme-accent, #3b82f6);\n");
        css.append("  color: white;\n");
        css.append("  box-shadow: var(--theme-shadow, 0 2px 8px rgba(0,0,0,0.1));\n");
        css.append("}\n\n");

        css.append(".btn-primary:hover {\n");
        css.append("  transform: translateY(-1px);\n");
        css.append("  box-shadow: 0 4px 12px rgba(0,0,0,0.15);\n");
        css.append("}\n\n");

        css.append(".btn-secondary {\n");
        css.append("  background-color: var(--theme-surface, #f8fafc);\n");
        css.append("  color: var(--theme-text, #1e293b);\n");
        css.append("  border: 1px solid var(--theme-border, #e2e8f0);\n");
        css.append("}\n\n");

        // Cards
        css.append(".card {\n");
        css.append("  background-color: var(--theme-surface, white);\n");
        css.append("  border: 1px solid var(--theme-border, #e2e8f0);\n");
        css.append("  border-radius: var(--theme-radius, 8px);\n");
        css.append("  padding: var(--theme-spacing, 1rem);\n");
        css.append("  box-shadow: var(--theme-shadow, 0 2px 8px rgba(0,0,0,0.1));\n");
        css.append("  transition: box-shadow 0.2s ease;\n");
        css.append("}\n\n");

        css.append(".card:hover {\n");
        css.append("  box-shadow: 0 4px 12px rgba(0,0,0,0.15);\n");
        css.append("}\n\n");

        // Features grid
        css.append(".features-grid {\n");
        css.append("  display: grid;\n");
        css.append("  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));\n");
        css.append("  gap: calc(var(--theme-spacing, 1rem) * 1.5);\n");
        css.append("  margin: 3rem 0;\n");
        css.append("}\n\n");

        // Utility classes
        css.append("/* Utility Classes */\n");
        css.append(".text-center { text-align: center; }\n");
        css.append(".text-left { text-align: left; }\n");
        css.append(".text-right { text-align: right; }\n\n");

        css.append(".mb-1 { margin-bottom: 0.5rem; }\n");
        css.append(".mb-2 { margin-bottom: var(--theme-spacing, 1rem); }\n");
        css.append(".mb-3 { margin-bottom: calc(var(--theme-spacing, 1rem) * 1.5); }\n");
        css.append(".mb-4 { margin-bottom: calc(var(--theme-spacing, 1rem) * 2); }\n\n");

        css.append(".mt-1 { margin-top: 0.5rem; }\n");
        css.append(".mt-2 { margin-top: var(--theme-spacing, 1rem); }\n");
        css.append(".mt-3 { margin-top: calc(var(--theme-spacing, 1rem) * 1.5); }\n");
        css.append(".mt-4 { margin-top: calc(var(--theme-spacing, 1rem) * 2); }\n\n");

        // Status styles
        css.append(".text-success { color: var(--theme-success, #22c55e); }\n");
        css.append(".text-warning { color: var(--theme-warning, #f59e0b); }\n");
        css.append(".text-error { color: var(--theme-error, #ef4444); }\n\n");

        // Responsive design
        css.append("/* Responsive Design */\n");
        css.append("@media (max-width: 768px) {\n");
        css.append("  .container {\n");
        css.append("    padding: 0 1rem;\n");
        css.append("  }\n");
        css.append("  \n");
        css.append("  h1 { font-size: 2.25rem; }\n");
        css.append("  h2 { font-size: 1.875rem; }\n");
        css.append("  h3 { font-size: 1.5rem; }\n");
        css.append("  \n");
        css.append("  .hero {\n");
        css.append("    padding: 3rem 0;\n");
        css.append("  }\n");
        css.append("}\n");
    }

    private void generateUtilitiesCSS(Path themeDir, Map<String, Object> aiTheme) throws IOException {
        StringBuilder utilities = new StringBuilder();
        
        utilities.append("/* Theme Utilities CSS */\n");
        utilities.append("/* Additional utility classes and animations */\n\n");

        // Animations
        utilities.append("@keyframes fadeIn {\n");
        utilities.append("  from { opacity: 0; transform: translateY(20px); }\n");
        utilities.append("  to { opacity: 1; transform: translateY(0); }\n");
        utilities.append("}\n\n");

        utilities.append("@keyframes slideIn {\n");
        utilities.append("  from { transform: translateX(-100%); }\n");
        utilities.append("  to { transform: translateX(0); }\n");
        utilities.append("}\n\n");

        // Animation classes
        utilities.append(".fade-in {\n");
        utilities.append("  animation: fadeIn 0.6s ease-out;\n");
        utilities.append("}\n\n");

        utilities.append(".slide-in {\n");
        utilities.append("  animation: slideIn 0.5s ease-out;\n");
        utilities.append("}\n\n");

        // Focus states for accessibility
        utilities.append("/* Accessibility */\n");
        utilities.append(".btn:focus,\n");
        utilities.append("input:focus,\n");
        utilities.append("textarea:focus {\n");
        utilities.append("  outline: 2px solid var(--theme-accent);\n");
        utilities.append("  outline-offset: 2px;\n");
        utilities.append("}\n\n");

        // Print styles
        utilities.append("/* Print Styles */\n");
        utilities.append("@media print {\n");
        utilities.append("  .no-print { display: none !important; }\n");
        utilities.append("  body { color: black !important; background: white !important; }\n");
        utilities.append("}\n");

        Files.write(themeDir.resolve("utilities.css"), utilities.toString().getBytes());
    }

    private void generateConfigFile(Path themeDir, Map<String, Object> aiTheme) throws IOException {
        StringBuilder config = new StringBuilder();

        config.append("// AI Generated Enhanced Theme Config\n");
        config.append("// Generated at: ").append(java.time.LocalDateTime.now()).append("\n");
        config.append("window.THEME_CONFIG = {\n");

        // Basic info
        appendConfigProperty(config, "templateId", aiTheme.get("templateId"));
        appendConfigProperty(config, "title", aiTheme.get("title"));
        appendConfigProperty(config, "description", aiTheme.get("description"));

        // Colors
        Map<String, String> colors = (Map<String, String>) aiTheme.get("colors");
        if (colors != null) {
            config.append("  colors: {\n");
            colors.forEach((key, value) -> 
                config.append("    ").append(key).append(": '").append(value).append("',\n"));
            removeTrailingComma(config);
            config.append("\n  },\n");
        }

        // Typography
        appendConfigSection(config, "typography", aiTheme.get("typography"));

        // Design
        appendConfigSection(config, "design", aiTheme.get("design"));

        // Content
        Map<String, Object> content = (Map<String, Object>) aiTheme.get("content");
        if (content != null) {
            config.append("  content: {\n");
            content.forEach((key, value) -> {
                if (value instanceof List) {
                    config.append("    ").append(key).append(": ").append(formatJavaScriptArray((List<?>) value)).append(",\n");
                } else {
                    config.append("    ").append(key).append(": '").append(escapeJavaScript(String.valueOf(value))).append("',\n");
                }
            });
            removeTrailingComma(config);
            config.append("\n  },\n");
        }

        // Layout
        appendConfigSection(config, "layout", aiTheme.get("layout"));

        // SEO
        appendConfigSection(config, "seo", aiTheme.get("seo"));

        removeTrailingComma(config);
        config.append("\n};\n\n");

        // Add helper functions
        config.append("// Helper functions\n");
        config.append("window.THEME_CONFIG.getColor = function(colorName) {\n");
        config.append("  return this.colors[colorName] || '#000000';\n");
        config.append("};\n\n");

        config.append("window.THEME_CONFIG.applyTheme = function() {\n");
        config.append("  const root = document.documentElement;\n");
        config.append("  Object.entries(this.colors).forEach(([key, value]) => {\n");
        config.append("    root.style.setProperty(`--theme-${key}`, value);\n");
        config.append("  });\n");
        config.append("};\n");

        Files.write(themeDir.resolve("config.js"), config.toString().getBytes());
    }

    private void appendConfigProperty(StringBuilder config, String key, Object value) {
        if (value != null) {
            config.append("  ").append(key).append(": '").append(escapeJavaScript(String.valueOf(value))).append("',\n");
        }
    }

    private void appendConfigSection(StringBuilder config, String sectionName, Object section) {
        if (section != null) {
            config.append("  ").append(sectionName).append(": ");
            if (section instanceof Map) {
                config.append("{\n");
                ((Map<String, Object>) section).forEach((key, value) -> {
                    if (value instanceof List) {
                        config.append("    ").append(key).append(": ").append(formatJavaScriptArray((List<?>) value)).append(",\n");
                    } else if (value instanceof Map) {
                        config.append("    ").append(key).append(": ").append(formatJavaScriptObject((Map<String, Object>) value)).append(",\n");
                    } else {
                        config.append("    ").append(key).append(": '").append(escapeJavaScript(String.valueOf(value))).append("',\n");
                    }
                });
                removeTrailingComma(config);
                config.append("\n  },\n");
            } else {
                config.append("'").append(escapeJavaScript(String.valueOf(section))).append("',\n");
            }
        }
    }

    private String formatJavaScriptArray(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item instanceof String) {
                sb.append("'").append(escapeJavaScript((String) item)).append("'");
            } else if (item instanceof Map) {
                sb.append(formatJavaScriptObject((Map<String, Object>) item));
            } else {
                sb.append(item);
            }
            if (i < list.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    private String formatJavaScriptObject(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (count > 0) sb.append(", ");
            sb.append(entry.getKey()).append(": ");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("'").append(escapeJavaScript((String) value)).append("'");
            } else if (value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append("'").append(escapeJavaScript(String.valueOf(value))).append("'");
            }
            count++;
        }
        sb.append("}");
        return sb.toString();
    }

    private void removeTrailingComma(StringBuilder sb) {
        if (sb.length() >= 2 && sb.substring(sb.length() - 2).equals(",\n")) {
            sb.setLength(sb.length() - 2);
        }
    }

    private String escapeJavaScript(String str) {
        return str.replace("\\", "\\\\")
                  .replace("'", "\\'")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    public boolean themeFilesExist(String subdomain) {
        Path themeDir = Paths.get(THEMES_DIR + subdomain);
        return Files.exists(themeDir.resolve("theme.css")) &&
               Files.exists(themeDir.resolve("config.js")) &&
               Files.exists(themeDir.resolve("utilities.css"));
    }

    public void deleteThemeFiles(String subdomain) {
        try {
            Path themeDir = Paths.get(THEMES_DIR + subdomain);
            if (Files.exists(themeDir)) {
                Files.walk(themeDir)
                     .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                         } catch (IOException e) {
                             logger.warn("Could not delete file: {}", path);
                         }
                     });
                logger.info("Theme files deleted for subdomain: {}", subdomain);
            }
        } catch (IOException e) {
            logger.error("Error deleting theme files for {}: {}", subdomain, e.getMessage());
        }
    }
}