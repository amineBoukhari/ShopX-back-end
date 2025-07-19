// AI Generated Enhanced Theme Config
// Generated at: 2025-07-19T13:11:22.042062200
window.THEME_CONFIG = {
  templateId: 'black-and-gold-theme',
  title: 'Luxury Black & Gold',
  description: 'Elegant black and gold themed website for a luxurious brand',
  colors: {
    secondary: '#D4AF37',
    border: '#E8E8E8',
    textSecondary: '#757575',
    surface: '#F8F8FF',
    background: '#FFFFFF',
    success: '#32CD32',
    warning: '#FFA500',
    text: '#1E1E1E',
    error: '#FF4500',
    accent: '#FFD700',
    primary: '#000000',
    info: '#000000'
  },
  typography: {
    fontFamily: '\'Playfair Display\', serif',
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
    ctaButton: 'Explore Now',
    features: ['Exquisite designs', 'Luxurious materials', 'Timeless elegance'],
    heroHeadline: 'Discover Luxury',
    footerText: '© 2024 Company Name. All rights reserved.',
    aboutText: 'We specialize in crafting exquisite pieces that define luxury and style.',
    heroSubtitle: 'Experience the elegance and sophistication of our exclusive products',
    aboutTitle: 'About Us'
  },
  layout: {
    header: {showLogo: true, sticky: true, style: 'split'},
    hero: {layout: 'centered', height: 'large'}
  },
  seo: {
    focusKeyword: 'luxury brand',
    keywords: ['luxury', 'elegance', 'sophistication'],
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
