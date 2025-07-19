// AI Generated Enhanced Theme Config
// Generated at: 2025-07-19T13:06:20.997661100
window.THEME_CONFIG = {
  templateId: 'fallback-theme',
  title: 'Mon Site Web',
  description: 'Site web généré automatiquement',
  colors: {
    secondary: '#6B7280',
    background: '#FFFFFF',
    accent: '#10B981',
    primary: '#3B82F6'
  },
  content: {
    tagline: 'Découvrez nos services',
    heroText: 'Bienvenue sur mon site',
    aboutText: 'Nous sommes une entreprise innovante.'
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
