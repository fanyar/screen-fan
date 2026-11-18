/*
* 在此处写
  import { createPinia } from 'pinia'
  const pinia = createPinia()
  export default pinia
  则需在main.js中引入
  import store from './store'
  app.use(store)
*
* 页面初始加载时，会加载所有的store.js,
* 如果模块太多则初始加载页面会慢有可能会出现空白
* 现在写法为按需加载
* import {useUserStore, useCommonStore} from "@/store"
*
*
* 官方推荐
* */

import { createPinia } from 'pinia'

export const pinia = createPinia()

export * from './modules/common.js';
export * from './modules/favorite.js';
export * from './modules/screen.js';
export * from './modules/user.js';
export * from './modules/menu.js';
export * from './modules/basic.js';
