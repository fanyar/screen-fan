# Project Agent Instructions

## Lanhu MCP recovery

When the user asks "我的lanhu-mcp 还在吗?" or says "lanhu又没了", first check whether the `lanhu` MCP server is available in the current Codex session and present under `[mcp_servers.lanhu]` in `/Users/fan/.codex/config.toml`.

If it is missing, run:

```bash
/bin/bash /Users/fan/Documents/Codex/2026-08-08/wiz/restore-lanhu-mcp.sh
```

Then verify that the config contains `[mcp_servers.lanhu]`, tell the user to restart Codex or start a new session, and remind them that the current session cannot hot-load the restored MCP server.

## Deploy and data sync

When the user says **部署**、**同步数据**、**部署并同步数据**, read and follow [docs/deploy-sync.md](docs/deploy-sync.md).

Default execution:

```bash
node screen-web/scripts/deploy-sync.mjs deploy --sync --message "<commit message>"
```

Do not report completion until GitHub Actions succeeds and the live mobile/data checks pass.
