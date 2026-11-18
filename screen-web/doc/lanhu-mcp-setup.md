# Lanhu MCP 接入说明

这份说明只整理这个项目里需要关注的文件，方便你用蓝湖链接做 1:1 还原时快速落地。

## 需要配置的文件

1. `.env.local`
   - 放蓝湖登录态和运行参数。
   - 关键变量：`LANHU_COOKIE`
   - 可选变量：`VIEWPORT_WIDTH`、`VIEWPORT_HEIGHT`、`HTTP_TIMEOUT`、`DATA_DIR`、`DEBUG`、`FEISHU_WEBHOOK_URL`
   - 这个文件已经在 `.gitignore` 里被忽略，适合放本地敏感信息。

2. `.env.example`
   - 这是模板文件，用来告诉团队哪些变量必须准备。
   - 不放真实 cookie，只保留键名和示例值。

3. `README.md`
   - 这里补一小段使用说明，提醒别人 `.env.local` 和蓝湖 MCP 的关系。

4. MCP 客户端配置
   - 这不在本仓库里，而是在你当前使用的 Codex / MCP 客户端里。
   - 需要把 `lanhu-mcp` 指向蓝湖链接，并确保它能读取本地 `.env.local`。

## 推荐做法

- 先把最新的蓝湖 cookie 写进 `.env.local` 的 `LANHU_COOKIE`
- 再用完整的 `stage` 链接或单页链接发给 `lanhu-mcp`
- 如果是单页精修，用 `detailDetach` 链接
- 如果是整套页面梳理，用 `stage` 链接

## 备注

这个仓库当前并没有内置蓝湖解析逻辑，所以蓝湖相关配置主要是给 MCP 客户端和本地环境准备的。
