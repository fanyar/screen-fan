/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        sx: {
          primary: 'var(--sx-primary)',
          'primary-hover': 'var(--sx-primary-hover)',
          'primary-active': 'var(--sx-primary-active)',
          soft: 'var(--sx-primary-soft)',
          accent: 'var(--sx-accent)',
          'accent-soft': 'var(--sx-accent-soft)',
          navy: 'var(--sx-navy)',
          canvas: 'var(--sx-canvas)',
          'nav-bg': 'var(--sx-nav-bg)',
          surface: 'var(--sx-surface)',
          line: 'var(--sx-line)',
          title: 'var(--sx-title)',
          body: 'var(--sx-body)',
          muted: 'var(--sx-muted)'
        }
      }
    }
  },
  plugins: []
}
