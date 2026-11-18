import {computed, ref} from 'vue'
import {defineStore} from "pinia"
import {getStore, removeStore, setStore} from "../../util/store.js";

export const useUserStore = defineStore('user', () => {
  let userName = ref('admin');
  let password = ref("123");
  let userInfo = ref(getStore({name: "userInfo"}) || null);

  const isLoggedIn = computed(() => Boolean(userInfo.value));

  const setUserInfo = (value) => {
    userInfo.value = value;
    setStore({name: "userInfo", content: value});
  };


  const MENU_LIST = [
    {name: '首页', path: '/home'},
    {
      name: '编辑大屏', children: [
        {name: '自定义大屏', path: '/test/screen/custom'},
        {name: '自定义大屏预览', path: '/test/screen/customView'},
      ]
    },
    {name: '预览组件', path: '/test/project/overView'},
    {name: '测试', path: '/test/project/index'},
    {name: '蓝湖首页', path: '/test/lanhu/index'},
    {name: 'vue3', path: '/test/project/vue3'},
    {name: 'gitTree', path: '/test/project/gitTree'},
    {name: 'canvas', path: '/test/project/canvas'},
    {name: '工序管理', path: '/test/beam/index'},
    {name: '旋转菜单', path: '/test/radialMenu'},
    {name: '测试', path: '/test/layout'},
  ]


  const editInfo = (value) => {
    userName.value += value
    password.value += value
  }

  const logout = () => {
    userInfo.value = null;
    removeStore({name: "userInfo"})
  }

  return {
    userName,
    password,
    userInfo,
    isLoggedIn,
    setUserInfo,
    MENU_LIST,
    editInfo,
    logout
  }
})
