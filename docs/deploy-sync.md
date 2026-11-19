# 部署与数据同步流程

这份流程适用于所有影响页面样式、平台数据、mock 数据、文章内容或图片的变更。目标是在手机视口和 GitHub Pages 线上环境都能看到最新结果。

## 触发规则

用户说以下内容时执行对应流程：

- **同步数据**：执行 `sync`，必要时继续本地检查，但不提交推送。
- **部署**：执行生产构建、移动端检查、提交推送、等待 Actions、线上验证。
- **部署并同步数据**：默认执行完整流程，等价于 `deploy --sync`。

项目入口文档是 [AGENTS.md](../AGENTS.md)。执行前先确认当前工作区只包含本次要发布的变更；不要把无关文件带进部署提交。

## 一键命令

在仓库根目录执行：

```bash
node screen-web/scripts/deploy-sync.mjs deploy --sync --message "Sync articles and update styles"
```

该命令会按顺序执行：

1. 启动或复用本地 `screen-admin` 后台。
2. 把后端数据同步到 `screen-web/public/mock/`。
3. 构建 GitHub Pages 生产包。
4. 用 Chrome DevTools 做 `390x844` 移动端检查。
5. 提交并推送 `main`。
6. 等待 `Deploy screen-web` 工作流成功。
7. 对比线上 `mock/manifest.json`。
8. 再次检查线上移动端列表和文章详情。

只同步数据、不部署：

```bash
node screen-web/scripts/deploy-sync.mjs sync
```

同步前重新导入掘金文章：

```bash
node screen-web/scripts/deploy-sync.mjs sync --juejin
```

只做本地检查，不提交推送：

```bash
node screen-web/scripts/deploy-sync.mjs check --sync
```

部署但跳过本次数据同步：

```bash
node screen-web/scripts/deploy-sync.mjs deploy --message "Update styles"
```

`screen-web` 内也可以使用：

```bash
cd screen-web
npm run check:mobile
npm run deploy:sync -- --message "Sync data and update styles"
```

## 数据同步细节

GitHub Pages 是静态站点，线上读取的是：

```text
screen-web/public/mock/data.json
screen-web/public/mock/manifest.json
```

后台数据变化后必须重新生成这两个文件：

```bash
cd screen-web
npm run sync:mock
```

如果后端没有运行，一键脚本会尝试启动 `screen-admin/start-server.sh`。

如果掘金文章内容或元数据需要重新抓取：

```bash
node screen-web/scripts/deploy-sync.mjs sync --juejin
```

注意：如果直接修改了 `juejin-articles.json` 中的正文，并希望已有 H2 数据升级，需要重启 `screen-admin` 后再执行 `sync:mock`。

## 移动端检查

自动化检查使用 `390x844`、DPR 2、移动端 UA 能力模拟。默认检查两个页面：

```text
/#/platform/articles
/#/platform/articles/<第一篇文章 id>
```

检查项：

- 文档宽度不超过视口宽度。
- 列表至少显示一篇文章。
- 列表能找到期望标题。
- 日期、标题、标签不被挤出屏幕。
- 详情标题与数据一致。
- 正文内容长度大于 20。
- 文章正文、代码块和图片没有横向撑破页面。
- 生成手机视口截图：
  - `/tmp/screen-fan-mobile-check/articles-390.png`
  - `/tmp/screen-fan-mobile-check/article-detail-390.png`

可以单独检查线上：

```bash
node screen-web/scripts/check-mobile.mjs --base-url https://fanyar.github.io/screen-fan/
```

如果本次改的是其他模块，例如游戏、模型、收藏页，需要额外手动检查对应路由；必要时修改 `check-mobile.mjs` 的默认路由或添加专项检查。

## 生产构建

GitHub Pages 构建参数固定为：

```bash
cd screen-web
VITE_BASE_PATH=/screen-fan/ VITE_USE_MOCK=true npm run build
```

一键脚本会自动使用这两个参数。不要把本地后台 API 地址或调试开关带入线上构建。

## 提交与推送

一键脚本会：

1. 打印 `git status --short`。
2. `git add -A`。
3. `git commit -m "<message>"`。
4. `git push origin main`。

提交说明必须描述本次变化，例如：

```bash
node screen-web/scripts/deploy-sync.mjs deploy --sync --message "Sync article data and fix mobile layout"
```

不要在没有提交说明时直接部署。

## GitHub Actions

推送后等待这个工作流成功：

```text
https://github.com/fanyar/screen-fan/actions
```

工作流名称：

```text
Deploy screen-web
```

一键脚本会通过 GitHub API 轮询当前 `HEAD` 对应的 run，最长等待 10 分钟。失败时先看失败日志，修复后再重新走完整流程，不要只反复重试。

## 线上验证

部署成功后检查：

主站：

```text
https://fanyar.github.io/screen-fan/
```

技术分享列表：

```text
https://fanyar.github.io/screen-fan/#/platform/articles
```

数据清单：

```text
https://fanyar.github.io/screen-fan/mock/manifest.json
```

一键脚本会对比本地和线上 `manifest.json` 的 `generatedAt`。如果不一致，说明 GitHub Pages 还没更新或构建发布失败。

线上移动端检查成功后才算部署完成。

## 常见问题

### 线上没有变化

按顺序检查：

1. `git push` 是否成功。
2. GitHub Actions 是否成功。
3. 本地 `public/mock/manifest.json` 是否更新。
4. 线上 `mock/manifest.json` 是否更新。
5. 手机浏览器是否缓存旧页面；可以用无痕窗口或强刷。

### 图片线上丢失

确认图片既存在于：

```text
screen-web/public/platform/...
```

也被 `sync:mock` 复制到：

```text
screen-web/public/mock/assets/...
```

### 后端数据已变，线上没变

GitHub Pages 不会读取本地 Java 后台。必须重新同步 mock 数据并部署。

### 移动端横向溢出

优先检查：

1. `body`、页面容器、卡片是否限制 `min-width: 0`。
2. 长标题是否允许换行。
3. 日期、按钮是否在 `768px` 以下换成独立行。
4. 代码块是否使用 `overflow-x: auto` 或移动端折行。
5. 图片和表格是否限制 `max-width: 100%`。
