import path from 'path';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, '.', '');
    return {
      server: {
        port: 5175,
        host: '0.0.0.0',
        hmr: {
          overlay: true,
        },
        watch: {
          // Exclude node_modules, dist, and other heavy directories
          ignored: [
            '**/node_modules/**',
            '**/dist/**',
            '**/build/**',
            '**/.git/**',
            '**/backend/**',
            '**/docs/**',
            '**/*.log',
          ],
          usePolling: false, // Disable polling for better performance
        },
      },
      plugins: [react()],
      define: {
        'process.env.API_KEY': JSON.stringify(env.GEMINI_API_KEY),
        'process.env.GEMINI_API_KEY': JSON.stringify(env.GEMINI_API_KEY)
      },
      resolve: {
        alias: {
          '@': path.resolve(__dirname, './src'),
        }
      },
      build: {
        rollupOptions: {
          output: {
            manualChunks: {
              'react-vendor': ['react', 'react-dom', 'react-router-dom'],
              'ui-vendor': ['@headlessui/react', 'react-hot-toast'],
              'calendar-vendor': ['@fullcalendar/react', '@fullcalendar/daygrid', '@fullcalendar/timegrid', '@fullcalendar/interaction'],
            }
          }
        },
        chunkSizeWarningLimit: 1000,
      }
    };
});
