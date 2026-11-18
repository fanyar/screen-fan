> 返回主文档：[claude.md](./claude.md)

# 已知问题与解决方案

## Tailwind CSS 样式不生效问题

### 原因

Tailwind 未配置 `content` 路径，未扫描到模板中的类名。

### 解决方案

在 `tailwind.config.cjs` 中配置：

```js
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}']
}
```

## Element Plus 样式覆盖 Tailwind 问题

### 原因

Element Plus 样式加载顺序在 Tailwind 之后，且选择器优先级相同。

### 解决方案

1. 使用 `unplugin-auto-import` 和 `unplugin-vue-components` 实现按需加载
2. 在 `element-plus.scss` 中使用组合选择器提高优先级
3. 使用 CSS 变量统一管理样式
4. 如需严格控制样式顺序，使用 `ElementPlusResolver({ importStyle: false })`，然后手动引入样式

## rem 布局与 Tailwind 的冲突

### 原因

修改根字体大小会影响 Tailwind 的默认 rem 样式（如 `text-base` = `1rem`）。

### 解决方案

- 使用 Tailwind v3 + `postcss-pxtorem`，让 `text-[14px]`、`w-[180px]` 等 px 工具类统一转换为 rem
- 根字号按屏幕高度设置：`1rem = viewportHeight / 10`
- 避免在需要自适应的页面使用 Tailwind 默认字号类（如 `text-base`），优先使用 `text-[14px]` 这类明确 px 工具类

## 滚动条样式

自定义滚动条样式在 `.scrollBar` 类中定义，可通过以下方式使用：

1. 直接添加 `.scrollBar` 类
2. 在 `element-plus.scss` 中为 `.el-main` 直接定义滚动条样式

## Terser 构建失败问题

### 原因

Vite 5.x 版本中 terser 已成为可选依赖，项目未安装导致构建失败。

### 解决方案

将构建工具从 terser 替换为 esbuild，在 `vite.config.js` 中修改：

```js
build: {
  minify: 'esbuild',
}
```

**优点**：esbuild 构建速度更快，无需额外安装依赖。

## 网易云音乐 API 跨域问题

### 原因

直接调用网易云音乐 API 会遇到跨域限制。

### 解决方案

在 `vite.config.js` 中配置代理：

```js
server: {
  proxy: {
    "/music-api": {
      target: "https://netease-cloud-music-api.vercel.app/",
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/music-api/, '')
    }
  }
}
```

API 服务层 (`src/api/music.js`) 统一使用 `/music-api` 前缀，由 Vite 代理转发请求。

### Mock 数据兜底

为确保 API 故障时页面仍可正常显示，首页组件 (`views/music/home.vue`) 实现了 Mock 数据兜底策略：

```js
const mockSongs = [
  {
    id: 1,
    name: "晴天",
    ar: [{ name: "周杰伦" }],
    al: { name: "叶惠美", picUrl: "..." },
    dt: 269000,
  },
  // ... 更多 Mock 数据
];
```

当 API 请求失败或返回数据为空时，自动使用 Mock 数据渲染页面。
