# screen-fan

包含两个项目：

- `screen-web/`：Vue 3 纯浏览展示端，发布到 GitHub Pages。
- `screen-admin/`：Spring Boot 本地内容管理后台，继续作为数据编辑源。

## 数据快照

先启动后台：

```bash
cd screen-admin
./start-server.sh
```

再导出当前公开数据：

```bash
cd ../screen-web
npm run sync:mock
```

导出的数据在 `screen-web/public/mock/`。后台更新内容后，重新执行 `npm run sync:mock`，提交并推送 `main` 分支，GitHub Actions 会自动发布。

## 本地开发

展示端默认走本地后端代理：

```bash
cd screen-web
npm run dev
```

生产构建默认启用 mock 数据：

```bash
cd screen-web
VITE_BASE_PATH=/screen-fan/ npm run build
```
