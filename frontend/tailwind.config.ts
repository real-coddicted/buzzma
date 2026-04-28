import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        neon: {
          red:    '#ff5c57',
          pink:   '#ff79c6',
          orange: '#ffb86c',
          yellow: '#f1fa8c',
          green:  '#50fa7b',
          cyan:   '#8be9fd',
          blue:   '#57c7ff',
          purple: '#bd93f9',
        },
        surface: {
          // dark surfaces
          'dark-base':    '#0a0a0a',
          'dark-raised':  '#111111',
          'dark-card':    '#161616',
          'dark-hover':   '#1e1e1e',
          'dark-border':  '#2a2a2a',
          'dark-muted':   '#3a3a3a',
          // light surfaces
          'light-base':   '#f4f5f7',
          'light-raised': '#ffffff',
          'light-card':   '#ffffff',
          'light-hover':  '#eef0f3',
          'light-border': '#e2e4e9',
          'light-muted':  '#cbd0d8',
        },
        ink: {
          'dark-primary':   '#e8eaf0',
          'dark-secondary': '#9196a1',
          'dark-muted':     '#565c66',
          'light-primary':  '#111318',
          'light-secondary':'#5a6172',
          'light-muted':    '#9198a6',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
      boxShadow: {
        'neon-red':    '0 0 8px 0 rgba(255, 92, 87, 0.4)',
        'neon-green':  '0 0 8px 0 rgba(80, 250, 123, 0.4)',
        'neon-cyan':   '0 0 8px 0 rgba(139, 233, 253, 0.4)',
        'neon-blue':   '0 0 8px 0 rgba(87, 199, 255, 0.4)',
        'neon-purple': '0 0 8px 0 rgba(189, 147, 249, 0.4)',
        'neon-orange': '0 0 8px 0 rgba(255, 184, 108, 0.4)',
        'card-dark':   '0 1px 3px 0 rgba(0,0,0,0.6), 0 1px 2px -1px rgba(0,0,0,0.6)',
        'card-light':  '0 1px 3px 0 rgba(0,0,0,0.06), 0 1px 2px -1px rgba(0,0,0,0.04)',
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'fade-in':    'fadeIn 0.2s ease-out',
        'slide-in':   'slideIn 0.25s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%':   { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideIn: {
          '0%':   { opacity: '0', transform: 'translateX(-8px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
      },
    },
  },
  plugins: [],
} satisfies Config
