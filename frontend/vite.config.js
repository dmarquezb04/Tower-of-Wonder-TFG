import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  // Base URL para assets dentro del bundle (imágenes importadas, etc.)
  base: '/assets/dist/',
  build: {
    // Output va al bind mount /app/dist → php/public/assets/dist en el host
    outDir: 'dist',
    emptyOutDir: true,
    rollupOptions: {
      output: {
        // Nombres fijos sin hash para que PHP pueda referenciarlos siempre igual
        entryFileNames: 'main.js',
        chunkFileNames: '[name].js',
        assetFileNames: (assetInfo) => {
          if (assetInfo.name?.endsWith('.css')) return 'main.css'
          return '[name][extname]'
        }
      }
    }
  }
})
