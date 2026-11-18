export const VILLA_ROUTE = '/platform/models/villa'

export const VILLA_VIEWS = [
  {
    id: 'overview',
    group: '外观',
    label: '总览',
    type: 'outdoor',
    roomId: '',
    camera: [18, 15, 22],
    target: [-2, 3, -1],
    info: '完整展示三层主体、退台檐廊、车库、庭院与无边水池。'
  },
  {
    id: 'courtyard',
    group: '外观',
    label: '庭院',
    type: 'outdoor',
    roomId: '',
    camera: [2, 2.8, 15],
    target: [-4, 1.8, 6.5],
    info: '入口影壁、石板步道、造型树、竹景与新中式矮墙。'
  },
  {
    id: 'pool',
    group: '外观',
    label: '泳池',
    type: 'outdoor',
    roomId: '',
    camera: [13, 5.2, 21],
    target: [1, 0.5, 11],
    info: '长方形无边水景池与南侧木平台。'
  },
  {
    id: 'garage',
    group: '外观',
    label: '车库',
    type: 'outdoor',
    roomId: '',
    camera: [14, 3.4, 8],
    target: [4.2, 1.6, -1],
    info: '双车位车库，配木格栅门头与石材车道。'
  },
  {
    id: 'f1-foyer',
    group: '一层',
    label: '门厅',
    type: 'interior',
    roomId: 'f1-foyer',
    camera: [-5.5, 2.3, 3.9],
    target: [-7.5, 1.2, 2.5],
    info: '入口玄关与客厅衔接，深木格栅界定入户序列。'
  },
  {
    id: 'f1-living',
    group: '一层',
    label: '客厅',
    type: 'interior',
    roomId: 'f1-living',
    camera: [-2.5, 3.4, 8.4],
    target: [-2.7, 0.9, 1.0],
    info: '挑高的主要会客区，浅色石材与胡桃木家具。'
  },
  {
    id: 'f1-tea',
    group: '一层',
    label: '茶室',
    type: 'interior',
    roomId: 'f1-tea',
    camera: [-5.3, 2.3, 0.3],
    target: [-6.9, 1.0, -1.4],
    info: '下沉茶席与格栅背景，保留庭院视线。'
  },
  {
    id: 'f1-dining',
    group: '一层',
    label: '餐厅',
    type: 'interior',
    roomId: 'f1-dining',
    camera: [-1.3, 2.3, 0.2],
    target: [-2.9, 1.0, -1.3],
    info: '长餐桌连接中西厨，黑色灯具与暖木柜体。'
  },
  {
    id: 'f1-kitchen',
    group: '一层',
    label: '厨房',
    type: 'interior',
    roomId: 'f1-kitchen',
    camera: [-1.3, 2.3, -2.5],
    target: [-2.9, 1.0, -4.4],
    info: '中厨与西厨岛台，哑光深灰柜门和石质台面。'
  },
  {
    id: 'f1-guest',
    group: '一层',
    label: '客卧',
    type: 'interior',
    roomId: 'f1-guest',
    camera: [-5.2, 2.3, -2.4],
    target: [-7.1, 1.0, -4.2],
    info: '一层客卧套房，靠庭院一侧设低窗和衣柜。'
  },
  {
    id: 'f2-master',
    group: '二层',
    label: '主卧',
    type: 'interior',
    roomId: 'f2-master',
    camera: [-4.5, 5.7, 3.6],
    target: [-6.6, 4.2, 1.0],
    info: '主卧套房含睡眠区、休闲角与衣帽区。'
  },
  {
    id: 'f2-bedroom',
    group: '二层',
    label: '次卧',
    type: 'interior',
    roomId: 'f2-bedroom',
    camera: [-0.2, 5.6, 3.8],
    target: [-2.5, 4.4, 0.9],
    info: '南侧次卧，柔和布艺与低饱和木饰面。'
  },
  {
    id: 'f2-family',
    group: '二层',
    label: '家庭厅',
    type: 'interior',
    roomId: 'f2-family',
    camera: [-4.4, 5.6, 0.3],
    target: [-6.6, 4.4, -1.9],
    info: '家庭厅连接两间卧室，适合阅读和日常聚会。'
  },
  {
    id: 'f2-study',
    group: '二层',
    label: '书房',
    type: 'interior',
    roomId: 'f2-study',
    camera: [-0.2, 5.6, 0.3],
    target: [-2.4, 4.4, -1.9],
    info: '书房整墙书架与工作桌，北向光线稳定。'
  },
  {
    id: 'f2-secondary',
    group: '二层',
    label: '卧室',
    type: 'interior',
    roomId: 'f2-secondary',
    camera: [-0.5, 5.6, -2.4],
    target: [-4.6, 4.4, -4.3],
    info: '北向卧室，衣柜与窗前坐位一体化。'
  },
  {
    id: 'f3-tea',
    group: '三层',
    label: '茶画室',
    type: 'interior',
    roomId: 'f3-tea',
    camera: [-4.2, 9.0, 3.3],
    target: [-6.5, 7.7, 0.5],
    info: '三层茶画室，长案与大尺度开窗面向露台。'
  },
  {
    id: 'f3-guest',
    group: '三层',
    label: '客房',
    type: 'interior',
    roomId: 'f3-guest',
    camera: [-0.2, 9.0, 3.3],
    target: [-2.4, 7.7, 0.5],
    info: '客房独立安静，配置收纳墙和休闲椅。'
  },
  {
    id: 'f3-activity',
    group: '三层',
    label: '活动室',
    type: 'interior',
    roomId: 'f3-activity',
    camera: [-4.2, 9.0, 0.1],
    target: [-6.4, 7.7, -2.0],
    info: '家庭活动室，可容纳影音、健身或亲子活动。'
  },
  {
    id: 'f3-terrace',
    group: '三层',
    label: '露台',
    type: 'interior',
    roomId: 'f3-terrace',
    camera: [-0.3, 9.1, 0.1],
    target: [-2.4, 7.8, -1.7],
    info: '退台露台设木平台、躺椅与绿植花池。'
  }
]

export const VILLA_SPECS = [
  {label: '风格', value: '新中式'},
  {label: '层数', value: '地上三层'},
  {label: '场地', value: '约 24m x 18m'},
  {label: '车库', value: '双车位'},
  {label: '泳池', value: '长方形无边池'},
  {label: '光照', value: '晴天白天'}
]

export const VILLA_VIEW_GROUPS = ['外观', '一层', '二层', '三层']
