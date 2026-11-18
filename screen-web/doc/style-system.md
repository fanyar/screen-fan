> 返回主文档：[claude.md](./claude.md)

# 样式体系

## Tailwind CSS 集成

项目使用 Tailwind CSS v3，通过 PostCSS 插件链集成：

```js
// postcss.config.js
plugins: [
  tailwindcss(),
  autoprefixer(),
  postcssPxtorem({ rootValue: 108 })
]
```

样式入口文件：`src/style/tailwind.css`，通过 `main.js` 引入。

模板扫描路径在 `tailwind.config.cjs` 中配置：

```js
content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}']
```

## Element Plus 按需加载

使用 `unplugin-auto-import` 和 `unplugin-vue-components` 实现自动按需导入：

```js
// vite.config.js
AutoImport({
  resolvers: [ElementPlusResolver()]
}),
Components({
  resolvers: [ElementPlusResolver()]
})
```

### 关键配置
- 移除了全局 `app.use(ElementPlus)`
- 移除了全局 `import 'element-plus/dist/index.css'`
- Element Plus 组件和样式按需自动导入
- 注意：如需控制样式加载顺序避免覆盖 Tailwind，可使用 `ElementPlusResolver({ importStyle: false })`，然后手动引入样式

## 样式优先级方案

### 问题
Element Plus 样式优先级高于 Tailwind CSS，导致 Tailwind 样式被覆盖。

### 解决方案
1. 在 `src/style/element-plus.scss` 中使用组合选择器覆盖 Element Plus 样式
2. 使用 CSS 变量定义滚动条样式，统一管理
3. 样式引入顺序：`style.scss` → `tailwind.css` → `theme.scss`

## 动态根字体大小（rem 布局）

在 `index.html` 中基于屏幕高度动态设置根字体大小：

```js
(function () {
  var DESIGN_HEIGHT = 1080;

  function setRootFontSize() {
    var viewportHeight = document.documentElement.clientHeight || window.innerHeight;
    document.documentElement.style.fontSize = viewportHeight / (DESIGN_HEIGHT / 108) + 'px';
  }

  setRootFontSize();
  window.addEventListener('resize', setRootFontSize);
  window.addEventListener('pageshow', function (event) {
    if (event.persisted) {
      setRootFontSize();
    }
  });
})();
```

### 缩放逻辑
- 屏幕高度 1080px：1rem = 108px（设计稿尺寸）
- 屏幕高度 540px：1rem = 54px（高度减半）
- 屏幕高度 2160px：1rem = 216px（高度加倍）
- 与 `postcss-pxtorem` 的 `rootValue: 108` 保持一致，108px 会转换为 1rem
- Tailwind 的 `text-[14px]` 会转换为约 `0.12963rem`，因此在 1080px 高度下显示为 14px，并随屏幕高度等比缩放

## 滚动条样式

自定义滚动条样式在 `.scrollBar` 类中定义，可通过以下方式使用：
1. 直接添加 `.scrollBar` 类
2. 在 `element-plus.scss` 中为 `.el-main` 直接定义滚动条样式

### CSS 变量定义
```css
:root {
  --scrollbar-width: 4px;
  --scrollbar-height: 8px;
  --scrollbar-thumb-bg: var(--tarin-base-background);
  --scrollbar-thumb-hover: #D0C9FF;
  --scrollbar-track-bg: #F5F3FF;
  --scrollbar-radius: 10px;
}
```
