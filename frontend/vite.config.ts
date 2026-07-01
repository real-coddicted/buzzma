import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'prompt',
      injectRegister: 'auto',

      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        cleanupOutdatedCaches: true,

        runtimeCaching: [
          // Google Fonts
          {
            urlPattern: /^https:\/\/fonts\.(?:googleapis|gstatic)\.com\/.*/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'google-fonts',
              expiration: {
                maxEntries: 20,
                maxAgeSeconds: 60 * 60 * 24 * 365, // 1 year
              },
            },
          },

          // Cloudflare Turnstile
          {
            urlPattern: /^https:\/\/challenges\.cloudflare\.com\/.*/i,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'cloudflare-turnstile',
            },
          },

          // Cache only relatively static reference API data.
          // Do NOT cache frequently changing business endpoints.
          {
            urlPattern: /^\/api\/(platforms|categories|campaign-types|countries|states|currencies)(\/.*)?$/i,
            handler: 'NetworkFirst',
            method: 'GET',
            options: {
              cacheName: 'reference-api-cache',
              expiration: {
                maxEntries: 30,
                maxAgeSeconds: 60 * 60 * 24, // 24 hours
              },
              networkTimeoutSeconds: 5,
              cacheableResponse: {
                statuses: [0, 200],
              },
            },
          },
        ],
      },

      manifest: {
        name: 'Buzzma',
        short_name: 'Buzzma',
        description: 'Buzzma Campaign and Ticket Dashboard',
        theme_color: '#0f172a',
        background_color: '#0f172a',
        display: 'standalone',
        orientation: 'portrait',
        start_url: '/',
        scope: '/',

        icons: [
          {
            src: 'pwa-192x192.png',
            sizes: '192x192',
            type: 'image/png',
          },
          {
            src: 'pwa-512x512.png',
            sizes: '512x512',
            type: 'image/png',
          },
          {
            src: 'pwa-512x512-maskable.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any maskable',
          },
        ],
      },

      devOptions: {
        enabled: true,
      },
    }),
  ],

  server: {
    host: '0.0.0.0',

    allowedHosts: ['.trycloudflare.com'],

    proxy: {
      '/api/events': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },

      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})