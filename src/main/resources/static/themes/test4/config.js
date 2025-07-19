// AI Generated Enhanced Theme Config
// Generated at: 2025-07-19T15:39:56.690248300
window.THEME_CONFIG = {
  templateId: 'gold-and-purple-theme',
  title: 'Luxury Creations',
  description: 'Discover the epitome of luxury with our exclusive gold and purple themed products. Unveil elegance and sophistication in every detail.',
  colors: {
    secondary: '#7E57C2',
    border: '#E2E8F0',
    textSecondary: '#64748B',
    surface: '#F3E5F5',
    background: '#FFFFFF',
    success: '#22C55E',
    warning: '#F59E0B',
    text: '#1E293B',
    error: '#EF4444',
    accent: '#8E24AA',
    primary: '#D4AF37',
    info: '#3B82F6'
  },
  typography: {
    fontFamily: '\'Inter\', system-ui, sans-serif',
    headingWeight: '700',
    lineHeight: '1.5',
    bodyWeight: '400'
  },
  design: {
    spacing: 'comfortable',
    mood: 'professional',
    borderRadius: '8px',
    shadows: 'subtle',
    style: 'modern'
  },
  content: {
    ctaButton: 'Shop Now',
    features: ['Opulent Gold Accents', 'Elegantly Embellished Designs', 'Sophisticated Purple Tones'],
    heroHeadline: 'Experience Luxury Unleashed',
    footerText: '© 2024 Luxury Creations. All rights reserved.',
    aboutText: 'At Luxury Creations, we curate exquisite pieces that embody luxury and elegance. Discover craftsmanship at its finest.',
    heroSubtitle: 'Indulge in the opulence of our gold and purple themed collection. Elevate your style with sophistication.',
    aboutTitle: 'About Us'
  },
  layout: {
    header: {showLogo: true, sticky: true, style: 'split'},
    hero: {layout: 'centered', height: 'large'}
  },
  seo: {
    focusKeyword: 'luxury creations',
    keywords: ['luxury', 'gold', 'purple'],
    language: 'en'
  }
};

// Helper functions
window.THEME_CONFIG.getColor = function(colorName) {
  return this.colors[colorName] || '#000000';
};

window.THEME_CONFIG.applyTheme = function() {
  const root = document.documentElement;
  Object.entries(this.colors).forEach(([key, value]) => {
    root.style.setProperty(`--theme-${key}`, value);
  });
};
