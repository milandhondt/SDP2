import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import tailwindcss from '@tailwindcss/vite';
import nodePolyfills from 'rollup-plugin-node-polyfills'; // Add this import

// https://vite.dev/config/
export default defineConfig({
  server: {
    allowedHosts: ['novafox.duckdns.org'],
    // optionally other server settings
  },
  plugins: [
    react(),
    tailwindcss(),
  ],
  build: {
    rollupOptions: {
      plugins: [
        nodePolyfills(), // Add this to include the polyfills
      ],
    },
  },
  resolve: {
    alias: {
      buffer: 'buffer/',
    },
  },
});
