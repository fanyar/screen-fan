import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import {fileURLToPath, URL} from 'node:url'

const backendTarget = process.env.BACKEND_TARGET || 'http://localhost:8666/'

export default defineConfig({
  base: process.env.VITE_BASE_PATH || '/',
  plugins: [
    vue()
  ],
  server: {
    port: 3000,
    proxy: {
      "/platform-api": {
        target: backendTarget,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/platform-api/, '')
      },
      "/uploads": {
        target: backendTarget,
        changeOrigin: true
      },
      "/api": {
        target: "https://zxpcs.com/",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
        bypass(req, res, option) {
          const proxyUrl = new URL(option.rewrite(req.url) || '', option.target)?.href || ''
          res.setHeader('x-req-proxyURL', proxyUrl)
        }
      },
      "/music-api": {
        target: "https://music.163.com/",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/music-api/, '')
      },
    }
  },
  /* css: {
     preprocessorOptions: {
       scss: {
         additionalData: '@use "./src/style/mixin.scss" as *;', // 加载全局样式，使用scss特性
       }
     },
   },*/
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  build: {
    target: 'es2015',
    cssTarget: 'chrome80',
    minify: 'esbuild',
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'echarts': ['echarts'],
          three: ['three']
        }
      }
    },
    chunkSizeWarningLimit: 1500
  },
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', 'element-plus', 'echarts']
  }
})
