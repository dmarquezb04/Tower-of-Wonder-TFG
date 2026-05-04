import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    // Output estándar para una SPA servida por Nginx
    outDir: 'dist',
    emptyOutDir: true,
    rollupOptions: {
      output: {
        // Mantenemos nombres fijos para consistencia si es necesario, 
        // aunque en una SPA pura podríamos usar hashes por defecto.
        entryFileNames: 'assets/[name].js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name].[ext]'
      }
    }
  }
})

