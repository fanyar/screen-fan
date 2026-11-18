> 返回主文档：[claude.md](./claude.md)

# 开发规范与最佳实践

## 技术选型

- 状态库统一使用 Pinia。
- UI 组件库统一使用 Element Plus。
- 使用 Tailwind CSS；当前项目版本为 v3。
- 不混用其他通用 UI 组件库，不用手写复杂交互替代组件库能力。

## 组件规范

- 组件文件名使用 PascalCase，名称应清晰描述功能。
- 弹窗、抽屉、确认框必须封装为独立组件，禁止在页面中手写遮罩 `div`。
- `props` 必须声明 `type` 和 `default`。
- 子组件禁止直接修改 `props`，需要变更时通过显式声明的 `emits` 通知父组件。
- `emits` 必须显式声明。
- 兄弟组件之间禁止互相引用，通信由共同父组件或 store 承担。
- `props` 禁止透传超过 2 层；确属跨页共享的数据改用 store。
- 页面组件的 `template + script` 超过 800 行必须拆分，`style` 部分不计入行数。
- 页面禁止 import 另一个页面；可复用内容应提取到组件、hooks 或工具模块。

## 样式规范

- 优先使用 Tailwind CSS 类。
- SFC 内样式必须写 `<style lang="scss" scoped>`。
- 禁止使用无 `lang` 的 `<style>`。
- 禁止使用 Less。
- 复杂样式写在 SFC 的 scoped SCSS 中。
- 主题色和可复用样式变量使用 CSS 变量统一定义。

## 状态管理

- 使用 Pinia 管理全局状态，模块按职责拆分。
- store 只存放登录态、用户信息、权限、跨页共享数据。
- 禁止把临时表单、局部弹窗状态放入 store。
- 组件内避免直接散落修改全局状态，应通过 store action 组织变更逻辑。

## 路由规范

- 新页面必须在 `src/router/index.js` 中使用 `() => import()` 懒加载注册。
- 路由命名清晰，便于维护。
- 登录拦截、权限校验统一放在路由守卫中处理。
- 页面只负责内容区，禁止重复实现全局布局。

## 编码红线

- 异步操作必须处理错误，禁止裸 `await`。
- 提交和高频操作必须防重复，可使用 loading、锁状态或防抖。
- 时间处理统一使用 dayjs；实际新增依赖前必须说明理由。
- 常量集中维护在 `src/const/`，禁止魔法字符串。
- 环境判断使用 `import.meta.env.DEV` 或 `import.meta.env.MODE`，禁止通过 `location.host` 猜测环境。
- 生产环境禁止输出 `console`。
- 新增依赖前必须说明理由，禁止引入与现有能力重复或体积过大的库，例如 moment、lodash 全量包。

## 性能优化

- 图片资源按需压缩并选择合适格式。
- 组件和路由按需加载。
- 避免不必要的重渲染。
- 大列表、图表和高频交互注意节流、防抖与销毁清理。

## Element Plus 按需加载

- 使用 `unplugin-auto-import` 和 `unplugin-vue-components` 自动按需导入。
- 如需控制样式加载顺序，使用 `ElementPlusResolver({ importStyle: false })`，再手动维护样式引入。

## 布局缩放

- 使用 `flex-1` 实现卡片自适应高度。
- 优先使用 `vh/vw` 或项目现有 rem 方案实现完全自适应。
- 避免脱离现有适配体系自行写固定缩放逻辑。

## 项目特色

### 数据可视化大屏

- 支持自定义大屏编辑。
- 提供基于 ECharts 的图表组件。
- 保持现有屏幕自适应能力。

### 交互式菜单

- 支持旋转菜单。
- 支持多级菜单导航。

### 动态布局

- 基于项目现有 rem 适配方案。
- 保持响应式设计。

### 现代化技术栈

- Vue 3 + Vite 5。
- Tailwind CSS v3。
- Element Plus。
- Pinia 状态管理。
