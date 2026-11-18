const networkCover = id => `https://images.unsplash.com/photo-${id}?auto=format&fit=crop&w=1200&q=80`

export const WORKS = [
  {
    id: 'work-1',
    title: '智慧工地总览大屏',
    cover: networkCover('1551288049-bebda4e38f71'),
    desc: '面向施工现场的实时数据总览，集成人员定位、设备运行与环境监测等多维指标，支持全屏投放与轮播模式。',
    tags: ['数据可视化', '大屏'],
    year: 2025
  },
  {
    id: 'work-2',
    title: 'T梁预制生产线监控',
    cover: networkCover('1581091226825-a6a2a5aee158'),
    desc: '对接产线 PLC 与传感器，实时展示工序节拍、质量合格率与产能趋势，异常自动高亮预警。',
    tags: ['工业互联网', 'ECharts'],
    year: 2025
  },
  {
    id: 'work-3',
    title: '销售实时监控看板',
    cover: networkCover('1611974789855-9c2a0a7236a3'),
    desc: '多渠道销售数据聚合，支持区域下钻与同比环比对比，帮助管理层快速定位增长点。',
    tags: ['BI', '数据大屏'],
    year: 2024
  },
  {
    id: 'work-4',
    title: '项目进度驾驶舱',
    cover: networkCover('1541888946425-d81bb19240f5'),
    desc: '以里程碑为主线的进度总览，甘特图与地图联动，直观呈现各标段施工状态与偏差。',
    tags: ['项目管理', 'GIS'],
    year: 2024
  },
  {
    id: 'work-5',
    title: '环境监测实时面板',
    cover: networkCover('1441974231531-c6227db76b6e'),
    desc: '扬尘、噪声与气象数据联动展示，超标自动推送告警，支持历史回放与趋势预测。',
    tags: ['物联网', '监测'],
    year: 2024
  },
  {
    id: 'work-6',
    title: '能耗管理分析平台',
    cover: networkCover('1473341304170-971dccb5ac1e'),
    desc: '水电燃气多能源分项计量，同比环比与峰谷分析，辅助园区精细化节能运营。',
    tags: ['能源', '分析'],
    year: 2023
  }
]

export const COMICS = [
  {
    id: 'comic-1',
    title: '像素星球',
    cover: networkCover('1550745165-9bc0b252726f'),
    status: '连载中',
    chapters: 12,
    desc: '一个关于像素世界冒险的短篇连载。'
  },
  {
    id: 'comic-2',
    title: '深夜代码',
    cover: networkCover('1515879218367-8466d910aaa4'),
    status: '完结',
    chapters: 8,
    desc: '程序员日常吐槽四格漫画合集。'
  },
  {
    id: 'comic-3',
    title: '猫与终端',
    cover: networkCover('1543852786-1cf6624b9987'),
    status: '连载中',
    chapters: 5,
    desc: '一只猫和一台旧终端的治愈故事。'
  }
]

export const MOVIES = [
  {
    id: 'movie-1',
    title: '银翼杀手 2049',
    cover: networkCover('1477959858617-67f85cf4f1df'),
    year: 2017,
    rating: 8.3,
    genre: '科幻',
    note: '摄影与美术的教科书级参考。'
  },
  {
    id: 'movie-2',
    title: '头号玩家',
    cover: networkCover('1592478411213-6153e4ebc07d'),
    year: 2018,
    rating: 8.7,
    genre: '冒险',
    note: '虚拟世界构建的灵感来源。'
  },
  {
    id: 'movie-3',
    title: '降临',
    cover: networkCover('1481627834876-b7833e8f5570'),
    year: 2016,
    rating: 7.9,
    genre: '剧情',
    note: '语言与时间线的叙事实验。'
  },
  {
    id: 'movie-4',
    title: '社交网络',
    cover: networkCover('1522071820081-009f0129c71c'),
    year: 2010,
    rating: 7.8,
    genre: '传记',
    note: '节奏与对白都值得反复看。'
  }
]

export const ARTICLES = [
  {
    id: 'article-1',
    title: 'Vue 3 组合式 API 在大屏项目中的实践',
    desc: '从 setup 语法糖到 composables 拆分，聊聊大屏组件如何做到逻辑复用且不失控。',
    tags: ['Vue3', '大屏'],
    date: '2025-06-12'
  },
  {
    id: 'article-2',
    title: 'Tailwind + pxtorem：大屏自适应的双轨方案',
    desc: '大屏走 rem 等比缩放，营销页走响应式断点，一套构建体系里两种适配模式并存的经验。',
    tags: ['Tailwind', '自适应'],
    date: '2025-04-08'
  },
  {
    id: 'article-3',
    title: 'ECharts 性能优化：十万级数据点的大屏渲染',
    desc: '数据抽样、增量渲染与 canvas 分层，让实时刷新的大屏在低配设备上也能流畅运行。',
    tags: ['ECharts', '性能'],
    date: '2024-11-20'
  },
  {
    id: 'article-4',
    title: '从零搭建一个数据可视化平台的前端架构',
    desc: '路由、状态、组件分层与构建优化的整体思路，以及踩过的那些坑。',
    tags: ['架构', '可视化'],
    date: '2024-09-05'
  }
]
