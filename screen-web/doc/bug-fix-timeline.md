> 返回主文档：[claude.md](./claude.md)

# Bug 修复时间线

本文整理的是仓库里能对上的修复记录，主要来源于 `git log`、`doc/known-issues.md`、`doc/build-optimization.md` 和项目追踪文档。  
有些提交同时包含“修复”和“优化”，这里按影响归类。

| 日期 | 修改内容 | 影响/对应问题 | 来源 |
| --- | --- | --- | --- |
| 2026-07-22 | 应用 `postcss-pxtorem`，同步调整 Tailwind 配置和页面布局 | 修复 rem 布局与 Tailwind 冲突、页面自适应问题 | `90cc30f`，`doc/known-issues.md` |
| 2026-07-10 | 恢复 `autofit` 初始化，改为绑定 `#app`，基准改为 1080 | 修复页面缩放/自适应未生效 | `2b95db3` |
| 2026-07-01 | 重构 music API 请求层，修正播放器取歌 URL 判空，修正收藏页专辑字段引用 | 修复音乐模块请求与展示问题 | `076582f` |
| 2026-07-01 | 记录 music 页面滚动条样式优化 | 修复滚动条样式不统一问题 | `.trae-cn/projects/music.md` |
| 2026-07-01 | 记录 ECharts 图表内存泄漏修复 | 修复图表性能与内存占用问题 | `doc/build-optimization.md`，`02904be` |

## 补充说明

- `known-issues.md` 更偏“问题说明 + 解决方案”。
- 这份文件更偏“按时间回看做过什么”。
- 如果后面要继续补，可以直接按日期追加，不必重排已有内容。
