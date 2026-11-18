# 项目协作规范

修改代码前，先阅读并遵循 [doc/best-practices.md](./doc/best-practices.md)。

## 必须遵守的规则

- 项目产品方向定位为“个人作品/内容平台”；功能分析、实现和优先级以官网首页、作品、漫画、电影、技术分享、内容管理和用户内容消费为主线。
- `/test/xxx` 路由相关页面属于实验或遗留演示页面，项目分析和功能规划时不纳入考虑范围，除非用户明确要求处理。
- 状态库统一使用 Pinia。
- UI 组件库统一使用 Element Plus。
- 项目启用 Tailwind CSS，当前版本为 v3。
- SFC 样式必须使用 `<style lang="scss" scoped>`；禁止使用无 `lang` 的 `<style>`，禁止使用 Less。
- 弹窗、抽屉、确认框必须封装为独立组件；页面禁止手写遮罩 `div`。
- 组件 `props` 必须声明 `type` 和 `default`；子组件禁止修改 props；`emits` 必须显式声明。
- 兄弟组件禁止互相引用；props 禁止透传超过 2 层，确属跨页数据改用 store。
- 页面 `template + script` 超过 800 行必须拆分，`style` 部分不计入行数。
- 页面禁止 import 另一个页面。
- store 只存放登录态、用户信息、权限、跨页数据；禁止存放临时表单。
- 新页面必须在 `src/router/index.js` 中懒加载注册；页面只写内容区，禁止重写全局布局。
- 登录拦截和权限校验统一放在路由守卫。
- 异步操作必须处理错误，禁止裸 `await`；提交必须防重复，高频操作必须节流或防抖。
- 时间统一使用 dayjs；如果依赖未安装，安装前需先确认理由。
- 常量集中放在 `src/const/`，禁止魔法字符串。
- 环境判断使用 `import.meta.env.DEV` 或 `import.meta.env.MODE`，禁止通过 `location.host` 猜测环境。
- 生产代码禁止输出 `console`。
- 新增依赖前必须说明理由；禁止引入重复能力或大体积库，例如 moment、lodash 全量包。
- 结构调整、新增依赖、规则变更前必须先讨论。
