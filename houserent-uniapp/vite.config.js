import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  define: {
    VITE_ROOT_DIR: JSON.stringify(process.cwd())
  },
  server: {
    hmr: {
      port: 3001
    }
  }
})
