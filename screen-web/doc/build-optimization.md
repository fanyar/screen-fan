> 返回主文档：[claude.md](./claude.md)

# Vite 构建优化

## 构建配置

```js
build: {
  target: 'es2015',
  cssTarget: 'chrome80',
  minify: 'terser',
  terserOptions: {
    compress: {
      drop_console: true,
      drop_debugger: true
    }
  },
  rollupOptions: {
    output: {
      manualChunks: {
        'element-plus': ['element-plus'],
        'echarts': ['echarts'],
        'vue-core': ['vue', 'vue-router', 'pinia']
      }
    }
  },
  chunkSizeWarningLimit: 1500
}
```

### 优化点

- 代码压缩：使用 terser 压缩，移除 console 和 debugger
- 分包策略：将 element-plus、echarts、vue-core 分别打包
- 目标浏览器：ES2015 + Chrome 80

## 依赖预构建

```js
optimizeDeps: {
  include: ["vue", "vue-router", "pinia", "element-plus", "echarts"];
}
```

## 开发服务器配置

```js
server: {
  port: 3000,
  proxy: {
    "/api": {
      target: "https://zxpcs.com/",
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, ''),
      bypass(req, res, option) {
        const proxyUrl = new URL(option.rewrite(req.url) || '', option.target)?.href || ''
        res.setHeader('x-req-proxyURL', proxyUrl)
      }
    }
  }
}
```

## 已实施的优化记录

### ECharts 优化

- 按需加载 ECharts 组件
- 修复图表内存泄漏问题

### 样式体系优化

- Tailwind CSS v4 集成
- Element Plus 按需加载
- 自定义滚动条样式

### 构建优化

- 代码压缩：移除 console 和 debugger
- 分包策略：减少首屏加载体积
