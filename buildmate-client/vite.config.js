import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    // Aligns with Docker-published frontend host port (see PORTS.md)
    port: 25173,
    proxy: {
      // All API traffic goes through Spring Cloud Gateway (JWT + X-API-KEY injection)
      '/api': {
        target: 'http://localhost:28080',
        changeOrigin: true,
      },
    },
  },
})
