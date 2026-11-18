/*
* 安装 npm install unplugin-auto-import unplugin-vue-components -D
* 配置 vite.config.js 
* 移除全局 Element Plus 导入
* import 'element-plus/dist/index.css'
* import ElementPlus from 'element-plus'
* app.use(ElementPlus)
*/


import {createApp} from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import './style/style.scss'
import './style/tailwind.css'
import './style/theme.scss'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from '@/router'
import {pinia} from './store'

const app = createApp(App)

//全局引入Element-icon
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(pinia)
app.use(ElementPlus, {locale: zhCn})
app.mount('#app')
