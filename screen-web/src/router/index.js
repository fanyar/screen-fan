import {createRouter, createWebHashHistory} from 'vue-router'
import {getStore} from "@/util/store"
import Layout from '@/page/index/layout.vue'

//创建路由
const Router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home',
    },
    {
      path: '/home',
      name: 'home',
      meta: {requireAuth: false, title: '首页'},
      component: () => import('@/views/home/index.vue'),
    },
    {
      path: '/platform',
      meta: {requireAuth: false},
      component: () => import('@/views/platform/layout.vue'),
      redirect: '/platform/works',
      children: [
        {
          path: 'works',
          name: '作品',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/works.vue'),
        },
        {
          path: 'works/:id',
          name: '作品详情',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/work-detail.vue'),
        },
        {
          path: 'models',
          name: '模型',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/models.vue'),
        },
        {
          path: 'models/house-tour',
          name: 'houseTour',
          meta: {requireAuth: false},
          component: () => import('@/views/test/house-tour.vue'),
        },
        {
          path: 'models/villa',
          name: 'villaModel',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/villa-model.vue'),
        },
        {
          path: 'models/suzhou-garden',
          name: 'suzhouGarden',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/suzhou-garden.vue'),
        },
        {
          path: 'models/:id',
          name: '模型详情',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/model-detail.vue'),
        },
        {
          path: 'comics',
          name: '漫画',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/comics.vue'),
        },
        {
          path: 'comics/:id',
          name: '漫画详情',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/comic-detail.vue'),
        },
        {
          path: 'comics/:comicId/chapters/:chapterId',
          name: '漫画阅读',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/comic-reader.vue'),
        },
        {
          path: 'movies',
          name: '电影',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/movies.vue'),
        },
        {
          path: 'movies/:id',
          name: '电影详情',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/movie-detail.vue'),
        },
        {
          path: 'anime',
          name: '动漫',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/anime.vue'),
        },
        {
          path: 'anime/:id',
          name: '动漫详情',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/anime-detail.vue'),
        },
        {
          path: 'articles',
          name: '技术分享',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/articles.vue'),
        },
        {
          path: 'articles/:id',
          name: '技术分享详情',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/article-detail.vue'),
        },
        {
          path: 'games',
          name: '游戏',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/games.vue'),
        },
        {
          path: 'games/kart',
          name: '跑跑卡丁车',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/kart-game.vue'),
        },
        {
          path: 'favorites',
          name: '我的收藏',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/favorites.vue'),
        },
        {
          path: 'history',
          name: '浏览历史',
          meta: {requireAuth: false},
          component: () => import('@/views/platform/history.vue'),
        },
        {
            path: 'profile',
            name: '个人中心',
            meta: {requireAuth: false},
            component: () => import('@/views/platform/profile.vue'),
        },
        {
          path: 'search',
          redirect: '/platform',
        }
      ]
    },
    {
      path: '/test',
      meta: {requireAuth: false},
      children: [
        {
          path: 'welcome',
          name: 'welcome',
          component: () => import('@/views/welcome/index.vue'),
        },
        {
          path: 'login',
          name: 'login',
          component: () => import('@/page/login/index.vue'),
        },
        {
          path: 'viewModule',
          name: 'viewModule',
          component: () => import('@/page/viewModule/index.vue'),
        },
        {
          path: 'beam',
          component: Layout,
          children: [
            {
              path: 'index',
              name: '工序管理',
              component: () => import('@/views/beam/index.vue'),
            }
          ]
        },
        {
          path: 'lanhu',
          component: Layout,
          children: [
            {
              path: 'index',
              name: '蓝湖首页',
              component: () => import('@/views/lanhu/index.vue'),
            }
          ]
        },
        {
          path: 'radialMenu',
          name: 'RadialMenu',
          component: () => import('@/page/carouselMenu/RadialMenu.vue'),
        },
        {
          path: 'screen',
          component: Layout,
          children: [
            {
              path: 'custom',
              name: '自定义大屏',
              component: () => import('@/views/customScreen/index.vue'),
            },
            {
              path: 'customView',
              name: '预览自定义大屏',
              component: () => import('@/views/customScreen/customViewScreen.vue'),
            }
          ]
        },
        {
          path: 'project',
          component: Layout,
          children: [
            {
              path: 'index',
              name: '测试',
              component: () => import('@/views/project/index.vue'),
            },
            {
              path: 'vue3',
              name: 'vue3',
              component: () => import('@/views/project/vue3.vue'),
            },
            {
              path: 'overView',
              name: 'overView',
              component: () => import('@/components/overView.vue'),
            },
            {
              path: 'gitTree',
              name: 'gitTree',
              component: () => import('@/screen-web/index.vue'),
            },
            {
              path: 'canvas',
              name: 'canvas',
              component: () => import('@/views/canvas/index.vue'),
            },
          ]
        },
        {
          path: 'layout',
          component: Layout,
          children: [
            {
              path: '',
              name: '布局',
              component: () => import('@/views/test/layout.vue'),
            }
          ]
        },
        {
          path: 'music',
          name: 'music',
          component: () => import('@/views/music/layout.vue'),
          redirect: '/test/music/home',
          children: [
            {
              path: 'home',
              name: '音乐首页',
              component: () => import('@/views/music/home.vue'),
            },
            {
              path: 'favorites',
              name: '音乐收藏',
              component: () => import('@/views/music/favorites.vue'),
            }
          ]
        }
      ]
    }
  ]
})

/*
* 导航守卫
* */
const WHITE_LIST = ['/home'];
const isWhiteList = (path) => WHITE_LIST.includes(path);

Router.beforeEach((to, form, next) => {
  if (isWhiteList(to.path)) {
    return next();
  }

  if (to.meta.requireAuth === false) {
    return next();
  }

  let userInfo = getStore({name: "userInfo"});
  if (!userInfo) {
    return next('/test/login');
  }

  next();
})
Router.afterEach(to => {
  let title = to.meta.title || to.name
  //根据当前的标签也获取label的值动态设置浏览器标题
  document.title = title;
})


export default Router
