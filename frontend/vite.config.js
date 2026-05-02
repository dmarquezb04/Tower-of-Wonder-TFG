import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  // Base URL para assets dentro del bundle (imágenes importadas, etc.)
  base: '/assets/dist/',
  build: {
    // Output va directamente donde PHP/nginx sirve los assets.
    // El contenedor node monta el proyecto raíz en /workspace,
    // y escribe aquí → php/public/assets/dist/ en el host.
    outDir: '../php/public/assets/dist',
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

