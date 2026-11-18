export const SUZHOU_GARDEN_ROUTE = '/platform/models/suzhou-garden'

export const SUZHOU_GARDEN_VIEWS = [
  {
    id: 'overview',
    group: '园林',
    label: '总览',
    camera: [58, 36, 68],
    target: [2, 2, -4],
    minDistance: 30,
    maxDistance: 115,
    info: '总览拙政园中部水面、假山、长廊、亭台与远山层次。'
  },
  {
    id: 'hall',
    group: '建筑',
    label: '远香堂',
    camera: [-11, 2.3, -29],
    target: [-5, 1.4, -22],
    minDistance: 1.5,
    maxDistance: 12,
    info: '进入远香堂厅内，观察梁架、屏门、坐席与面向水面的视野。'
  },
  {
    id: 'bridge',
    group: '建筑',
    label: '小飞虹',
    camera: [-14, 2.0, -2.8],
    target: [2, 1.3, 1.2],
    minDistance: 1.5,
    maxDistance: 14,
    info: '沿小飞虹廊桥穿行，水面倒影穿过木构花格与波形廊顶。'
  },
  {
    id: 'pavilion',
    group: '建筑',
    label: '荷风四面亭',
    camera: [10, 2.2, 5.2],
    target: [-4, 1.1, 0.2],
    minDistance: 1.5,
    maxDistance: 12,
    info: '立于水面亭中，四面可观荷花、曲桥、假山与岸畔建筑。'
  },
  {
    id: 'boat-house',
    group: '建筑',
    label: '香洲',
    camera: [34, 5.0, 30],
    target: [21, 1.8, 17],
    minDistance: 3,
    maxDistance: 26,
    info: '近看石舫式建筑，前舱、平台、主厅与层叠屋檐形成船形轮廓。'
  },
  {
    id: 'water',
    group: '景观',
    label: '水面',
    camera: [36, 8.5, 31],
    target: [2, .2, -2],
    minDistance: 8,
    maxDistance: 55,
    info: '低视角观察湖面波纹、荷叶、倒影与岸线石砌细节。'
  },
  {
    id: 'rockery',
    group: '景观',
    label: '假山',
    camera: [-24, 8.5, 12],
    target: [-2, 2.2, -9],
    minDistance: 5,
    maxDistance: 40,
    info: '从山径方向回望中部园景，黄石假山与树木遮蔽形成框景。'
  },
  {
    id: 'mountain',
    group: '景观',
    label: '远山',
    camera: [-36, 13, 38],
    target: [4, 8, -58],
    minDistance: 16,
    maxDistance: 70,
    info: '穿过园林屋脊和树梢，看向湿润雾气中的江南远山。'
  }
]

export const SUZHOU_GARDEN_VIEW_GROUPS = ['园林', '建筑', '景观']

export const SUZHOU_GARDEN_TOUR_ORDER = [
  'overview',
  'hall',
  'bridge',
  'pavilion',
  'water',
  'rockery',
  'boat-house',
  'mountain'
]

export const SUZHOU_GARDEN_SPECS = [
  {label: '参考', value: '拙政园中部'},
  {label: '核心', value: '远香堂 · 小飞虹'},
  {label: '景观', value: '水面 · 假山 · 树木'},
  {label: '漫游', value: '固定视角 + 路径'},
  {label: '氛围', value: '江南清晨薄雾'}
]
