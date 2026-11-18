# Vue 3 + Vite

This template should help get you started developing with Vue 3 in Vite. The template uses Vue 3 `<script setup>` SFCs,
check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

## Recommended IDE Setup

- [VS Code](https://code.visualstudio.com/)
    + [Vue - Official](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (previously Volar) and disable
      Vetur

## Lanhu MCP

如果你要用蓝湖链接做 1:1 还原，先看 [doc/lanhu-mcp-setup.md](./doc/lanhu-mcp-setup.md)。

## 建立依赖关系，把子项目克隆到父项目里面

# 如果是想放在根目录下

# git subtree add --prefix=screen-web https://gitee.com/fanyar/customScreen.git master

# 如果是想放在根目录下的src里面（建议放在src目录下）

# git subtree add --prefix=src/screen-web https://gitee.com/fanyar/customScreen.git master

## 删除子项目

# 直接删除然后提交代码即可

## 更新子项目

# 可在父项目里面直接修改子项目文件，然后提交（注意：--prefix后面跟的相对地址）

# 先提交父项目，再提交子项目

# git subtree push --prefix=src/screen-web https://gitee.com/fanyar/customScreen.git master

## Element-icon 大项目按需加载， 中小项目全局加载

## 按需加载全局引入Element-icon

# import { ElIcon } from 'element-plus'

# import { CloseBold } from '@element-plus/icons-vue'

# <ElIcon><CloseBold /></ElIcon>



