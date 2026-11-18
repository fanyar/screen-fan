---
name: "项目追踪"
description: "多项目进度追踪。当用户说'项目X 开始/结束'或要求追踪项目功能/Bug时调用。"
---

# 项目追踪

一个用于追踪多个项目进度、功能和Bug的工具。

## 命令

| 命令                           | 操作               |
| ------------------------------ | ------------------ |
| `项目X 开始`                   | 显示项目X进度报告  |
| `项目X 结束`                   | 保存项目X当前进度  |
| `项目列表`                     | 显示所有项目概览   |
| `项目X 新增功能 功能名`        | 向项目X添加新功能  |
| `项目X 新增Bug Bug描述`        | 向项目X添加Bug     |
| `项目X 完成功能 功能名`        | 将功能标记为已完成 |
| `项目X 修复Bug Bug名`          | 将Bug标记为已修复  |
| `项目X 优先级 功能名 高/中/低` | 设置优先级         |
| `项目X 备注 功能名 备注内容`   | 添加备注           |

## 状态符号

- `✅` 已完成
- `⏳` 开发中
- `❌` 未开始
- `🐛` Bug（待修复）
- `🔧` Bug（已修复）

## 数据存储

所有项目数据存储在项目目录中：

- **JSON**：`{projectRoot}/.trae-cn/projects.json`（用于命令操作）
- **Markdown**：`{projectRoot}/.trae-cn/projects/项目X.md`（用于详细信息）

**重要**：`projectRoot` 由当前工作目录（cwd）决定。每个项目在 `src` 同级目录下有自己的 `.trae-cn` 文件夹。

## 工作流程

### 1. 项目开始（项目X 开始）

当用户说"项目X 开始"时：

1. 获取当前工作目录（cwd）作为 `projectRoot`
2. 读取 `{projectRoot}/.trae-cn/projects.json`
3. 查找项目X数据
4. 生成进度报告：

   ```
   📌 项目X - 进度报告

   ✅ 已完成：功能1、功能2
   ⏳ 开发中：功能3
   ❌ 未开始：功能4、功能5
   🐛 待修复Bug：Bug1

   👉 建议继续：功能3
   最后更新：2026-06-30 10:00
   ```

5. 设置当前项目上下文为X

### 2. 项目结束（项目X 结束）

当用户说"项目X 结束"时：

1. 获取当前工作目录（cwd）作为 `projectRoot`
2. 从对话中读取项目X当前状态
3. 更新 `{projectRoot}/.trae-cn/projects.json`
4. 更新 `{projectRoot}/.trae-cn/projects/项目X.md`
5. 确认保存：
   ```
   ✅ 已保存项目X进度：
   - 功能3：⏳ 开发中
   - 功能4：❌ 未开始
   保存时间：2026-06-30 14:00
   ```

### 3. 添加功能/Bug

当用户说"项目X 新增功能 功能名"时：

1. 获取当前工作目录（cwd）作为 `projectRoot`
2. 读取 `{projectRoot}/.trae-cn/projects.json`
3. 添加新功能，状态为 `❌`
4. 更新JSON和Markdown文件
5. 确认添加成功

## 示例（音乐项目）

```json
{
  "projects": {
    "music": {
      "name": "音乐网站",
      "features": {
        "首页三栏布局": {
          "status": "✅",
          "priority": "高",
          "completedAt": "2026-06-28"
        },
        "我的收藏": {
          "status": "✅",
          "priority": "高",
          "completedAt": "2026-06-28"
        },
        "全局播放器": {
          "status": "✅",
          "priority": "高",
          "completedAt": "2026-06-29"
        },
        "搜索功能": {
          "status": "⏳",
          "priority": "高",
          "startedAt": "2026-06-30"
        },
        "歌词显示": { "status": "❌", "priority": "高", "note": "" },
        "播放模式切换": { "status": "❌", "priority": "中", "note": "" }
      },
      "bugs": {
        "页面宽度异常": {
          "status": "🔧",
          "priority": "高",
          "fixedAt": "2026-06-30",
          "solution": "添加 width: 100vw"
        }
      },
      "lastUpdated": "2026-06-30 10:00",
      "currentFocus": "搜索功能"
    }
  }
}
```

## 重要提示

- 显示报告前务必读取JSON文件
- 保存时务必同时更新JSON和Markdown文件
- 支持中英文项目名称
- 保持Markdown文件易读，包含详细备注
