import {defineStore} from "pinia"

export const useBasicStore = defineStore("basic", () => {
  const MENU_DATA = [
    {
      id: 'm1',
      label: '项目介绍',
      icon: 'projectInfo',
      active: false,
      children: [
        {
          id: 'm1-1',
          label: '宣传视频',
          route: '/project/project-intro/promotional-video',
          icon: 'promotionalVideo',
          active: false,
        },
        {
          id: 'm1-2',
          label: '项目特色',
          route: '/project/project-intro/highlights',
          icon: 'projectHighlights',
          active: false,
        },
        {
          id: 'm1-3',
          label: '生产总量',
          route: '/project/project-intro/total-production',
          icon: 'totalProduction',
          active: false,
        },
        {
          id: 'm1-4',
          label: '工期计划',
          route: '/project/project-intro/construction-progress',
          icon: 'projectSchedule',
          active: false,
        },
        {
          id: 'm1-5',
          label: '工艺介绍',
          route: '/project/project-intro/key-processes',
          icon: 'processIntroduction',
          active: false,
        },
        {
          id: 'm1-6',
          label: '厂区介绍',
          route: '/project/project-intro/plant-introduction',
          icon: 'floorPlan',
          active: false,
        },
      ],
    },
    {
      id: 'm2',
      label: '智慧化决策',
      icon: 'zhhjc',
      active: false,
      children: [
        {
          id: 'm2-1',
          label: '总体进度',
          route: '/project/smart-decision/overall-progress',
          icon: 'ztjd',
          active: false,
        },
        {
          id: 'm2-2',
          label: '钢筋加工',
          route: '/project/smart-decision/rebar-processing',
          icon: 'gjjg',
          active: false,
        },
        {
          id: 'm2-3',
          label: '混凝土生产',
          route: '/project/smart-decision/concrete-production',
          icon: 'hltsc',
          active: false,
        },
        {
          id: 'm2-4',
          label: 'T梁预制',
          route: '/project/smart-decision/t-beam-prefabrication',
          icon: 'tlyz',
          active: false,
        },
        {
          id: 'm2-5',
          label: 'π梁预制',
          route: '/project/smart-decision/pi-beam-prefabrication',
          icon: 'plyz',
          active: false,
        },
        {
          id: 'm2-6',
          label: '小构预制',
          route: '/project/smart-decision/small-component-prefabrication',
          icon: 'xgyz',
          active: false,
        },
        {
          id: 'm2-7',
          label: '护栏预制',
          route: '/project/smart-decision/jersey-barrier-prefabrication',
          icon: 'hlyz',
          active: false,
        },
      ],
    },
    {
      id: 'm3',
      label: '数字化管理',
      icon: 'szhgl',
      active: false,
      children: [
        {
          id: 'm3-1',
          label: '人员管理',
          route: '/project/digital-management/personnel-management',
          icon: 'rygl',
          active: false,
        },
        {
          id: 'm3-2',
          label: '设备管理',
          route: '/project/digital-management/device-management',
          icon: 'sbgl',
          active: false,
        },
        {
          id: 'm3-3',
          label: '物料管理',
          route: '/project/digital-management/material-management',
          icon: 'wlgl',
          active: false,
        },
        {
          id: 'm3-4',
          label: '安全管理',
          route: '/project/digital-management/safety-management',
          icon: 'aqgl',
          active: false,
        },
        {
          id: 'm3-5',
          label: '进度管理',
          route: '/project/digital-management/progress-management',
          icon: 'jdgl',
          active: false,
        },
        {
          id: 'm3-6',
          label: '视频和AI',
          route: '/project/digital-management/video-and-ai',
          icon: 'spai',
          active: false,
        },
        {
          id: 'm3-7',
          label: '无人机',
          route: '/project/digital-management/drone',
          icon: 'wrj',
          active: false,
        },
        {
          id: 'm3-8',
          label: '绿色施工',
          route: '/project/digital-management/green-construction',
          icon: 'lssg',
          active: false,
        },
      ],
    },
    {
      id: 'm4',
      label: '工业化生产',
      icon: 'gyhgl',
      active: false,
      children: [
        {
          id: 'm4-1',
          label: '工艺模拟',
          route: '/project/industrial-production/process-simulation',
          icon: 'gymn',
          active: false,
        },
        {
          id: 'm4-2',
          label: '生产动态',
          route: '/project/industrial-production/production-dynamics',
          icon: 'scdt',
          active: false,
        },
        {
          id: 'm4-3',
          label: 'T梁生产线',
          route: '/project/industrial-production/t-beam-production-line',
          icon: 'tlyz',
          active: false,
        },
        {
          id: 'm4-4',
          label: 'π梁生产线',
          route: '/project/industrial-production/pi-beam-production-line',
          icon: 'plyz',
          active: false,
        },
        {
          id: 'm4-5',
          label: '混凝土拌合站',
          route: '/project/industrial-production/concrete-batching-station',
          icon: 'hltbhz',
          active: false,
        },
        {
          id: 'm4-6',
          label: '钢筋加工厂',
          route: '/project/industrial-production/rebar-processing-factory',
          icon: 'gjjg',
          active: false,
        },
        {
          id: 'm4-7',
          label: '小构生产线',
          route: '/project/industrial-production/small-component-production-line',
          icon: 'xgyz',
          active: false,
        },
        {
          id: 'm4-8',
          label: '护栏生产线',
          route: '/project/industrial-production/jersey-barrier-production-line',
          icon: 'hlyz',
          active: false,
        },
      ],
    },
    {
      id: 'm5',
      label: '系统设置',
      icon: 'xtsz',
      active: false,
      children: [
        {
          id: 'm5-1',
          label: '漫游设置',
          route: '/project/system-setting/roaming-setting',
          icon: 'roamIcon',
          active: false,
        },
        {
          id: 'm5-2',
          label: '自动播报',
          route: '/project/system-setting/auto-broadcast',
          icon: 'automatic',
          active: false,
        },
      ],
    },
  ];
  const MENU_LEVEL_FOUR = [
    {
      activeStatus: "1",
      createBy: "13647215045",
      createTime: "2026-01-11 10:40:47",
      factoryId: "1935513823569428482",
      id: "2010180055145762817",
      lineCode: "C-SCX-04",
      lineLeader: "",
      lineName: "30米T梁产线",
      lockedState: "0",
      modeOption: "",
      notTwinClockwiseBeamType: "",
      notTwinReverseBeamType: "",
      processId: "2010175800229875714",
      productMethod: "2",
      productionMode: "1",
      tenantId: "1935513823326158849",
      twinClockwiseBeamType: "30MTL-bb-1-OUT_BB,30MTL-bb-1-IN_BB,30MTL-bb-2-OUT_BB,30MTL-bb-2-IN_BB,30MTL-bz-1,30MTL-bz-2,30MTL-zz-1,30MTL-zz-2,30MTL-zb-1-OUT_BB,30MTL-zb-1-IN_BB,30MTL-zb-2-OUT_BB,30MTL-zb-2-IN_BB",
      twinReverseBeamType: "",
      updateBy: "13647215045",
      updateTime: "2026-01-11 10:40:47",
    },
    {
      activeStatus: "1"
      ,
      createBy: "13647215045"
      ,
      createTime: "2026-01-16 09:37:06"
      ,
      factoryId: "1935513823569428482"
      ,
      id: "2011975967694245889"
      ,
      lineCode: "C-SCX-05"
      ,
      lineLeader: ""
      ,
      lineName: "16米T梁产线"
      ,
      lockedState: "0"
      ,
      modeOption: ""
      ,
      notTwinClockwiseBeamType: ""
      ,
      notTwinReverseBeamType: ""
      ,
      processId: "2010175800229875714"
      ,
      productMethod: "2"
      ,
      productionMode: "1"
      ,
      tenantId: "1935513823326158849"
      ,
      twinClockwiseBeamType: "30MTL-bb-1-OUT_BB,30MTL-bb-1-IN_BB,30MTL-bb-2-OUT_BB,30MTL-bb-2-IN_BB,30MTL-zb-1-OUT_BB,30MTL-zb-1-IN_BB,30MTL-zb-2-OUT_BB,30MTL-zb-2-IN_BB"
      ,
      twinReverseBeamType: ""
      ,
      updateBy: "13647215045"
      ,
      updateTime: "2026-01-16 09:37:06"
    },
    {
      activeStatus: "1"
      , createBy: "13647215045"
      , createTime: "2026-01-16 09:37:35"
      , factoryId: "1935513823569428482"
      , id: "2011976087194161153"
      , lineCode: "C-SCX-06"
      , lineLeader: ""
      , lineName: "20米T梁产线"
      , lockedState: "0"
      , modeOption: ""
      , notTwinClockwiseBeamType: ""
      , notTwinReverseBeamType: ""
      , processId: "2010175800229875714"
      , productMethod: "2"
      , productionMode: "1"
      , tenantId: "1935513823326158849"
      , twinClockwiseBeamType: "30MTL-bz-1,30MTL-bz-2,30MTL-zz-1,30MTL-zz-2"
      , twinReverseBeamType: ""
      , updateBy: "13647215045"
      , updateTime: "2026-01-16 09:37:35"
    },
    {
      activeStatus: "1"
      ,
      createBy: "13647215045"
      ,
      createTime: "2026-01-16 09:38:06"
      ,
      factoryId: "1935513823569428482"
      ,
      id: "2011976218563956738"
      ,
      lineCode: "C-SCX-07"
      ,
      lineLeader: ""
      ,
      lineName: "40米T梁产线"
      ,
      lockedState: "0"
      ,
      modeOption: ""
      ,
      notTwinClockwiseBeamType: ""
      ,
      notTwinReverseBeamType: ""
      ,
      processId: "2010175800229875714"
      ,
      productMethod: "2"
      ,
      productionMode: "1"
      ,
      tenantId: "1935513823326158849"
      ,
      twinClockwiseBeamType: "30MTL-bz-1,30MTL-bz-2,30MTL-zz-1,30MTL-zz-2,30MTL-bb-1-OUT_BB,30MTL-bb-1-IN_BB,30MTL-bb-2-OUT_BB,30MTL-bb-2-IN_BB,30MTL-zb-1-OUT_BB,30MTL-zb-1-IN_BB,30MTL-zb-2-OUT_BB,30MTL-zb-2-IN_BB"
      ,
      twinReverseBeamType: ""
      ,
      updateBy: "13647215045"
      ,
      updateTime: "2026-01-16 09:38:15"
    },
    {
      activeStatus: "1"
      ,
      createBy: "13647215045"
      ,
      createTime: "2026-01-16 09:38:46"
      ,
      factoryId: "1935513823569428482"
      ,
      id: "2011976386453557249"
      ,
      lineCode: "C-SCX-08"
      ,
      lineLeader: ""
      ,
      lineName: "36米T梁产线"
      ,
      lockedState: "0"
      ,
      modeOption: ""
      ,
      notTwinClockwiseBeamType: ""
      ,
      notTwinReverseBeamType: ""
      ,
      processId: "2010175800229875714"
      ,
      productMethod: "2"
      ,
      productionMode: "1"
      ,
      tenantId: "1935513823326158849"
      ,
      twinClockwiseBeamType: "30MTL-bb-1-OUT_BB,30MTL-bb-1-IN_BB,30MTL-bb-2-OUT_BB,30MTL-bb-2-IN_BB,30MTL-bz-1,30MTL-bz-2,30MTL-zz-1,30MTL-zz-2,30MTL-zb-1-OUT_BB,30MTL-zb-1-IN_BB,30MTL-zb-2-OUT_BB,30MTL-zb-2-IN_BB"
      ,
      twinReverseBeamType: ""
      ,
      updateBy: "13647215045"
      ,
      updateTime: "2026-01-16 09:38:46"
    },
    {
      activeStatus: "1"
      ,
      createBy: "13647215045"
      ,
      createTime: "2026-01-16 09:39:24"
      ,
      factoryId: "1935513823569428482"
      ,
      id: "2011976544494931971"
      ,
      lineCode: "C-SCX-09"
      ,
      lineLeader: ""
      ,
      lineName: "13米T梁"
      ,
      lockedState: "0"
      ,
      modeOption: ""
      ,
      notTwinClockwiseBeamType: ""
      ,
      notTwinReverseBeamType: ""
      ,
      processId: "2010175800229875714"
      ,
      productMethod: "2"
      ,
      productionMode: "1"
      ,
      tenantId: "1935513823326158849"
      ,
      twinClockwiseBeamType: "30MTL-bb-1-OUT_BB,30MTL-bb-1-IN_BB,30MTL-bb-2-OUT_BB,30MTL-bb-2-IN_BB,30MTL-bz-1,30MTL-bz-2,30MTL-zz-1,30MTL-zz-2,30MTL-zb-1-OUT_BB,30MTL-zb-1-IN_BB,30MTL-zb-2-OUT_BB,30MTL-zb-2-IN_BB"
      ,
      twinReverseBeamType: ""
      ,
      updateBy: "13647215045"
      ,
      updateTime: "2026-01-16 09:39:24"
    },
    {
      activeStatus: "1"
      ,
      createBy: "13647215045"
      ,
      createTime: "2026-01-16 09:39:47"
      ,
      factoryId: "1935513823569428482"
      ,
      id: "2011976641026838530"
      ,
      lineCode: "C-SCX-10"
      ,
      lineLeader: ""
      ,
      lineName: "15.5米T梁"
      ,
      lockedState: "0"
      ,
      modeOption: ""
      ,
      notTwinClockwiseBeamType: ""
      ,
      notTwinReverseBeamType: ""
      ,
      processId: "2010175800229875714"
      ,
      productMethod: "2"
      ,
      productionMode: "1"
      ,
      tenantId: "1935513823326158849"
      ,
      twinClockwiseBeamType: "30MTL-bb-1-OUT_BB,30MTL-bb-1-IN_BB,30MTL-bb-2-OUT_BB,30MTL-bb-2-IN_BB,30MTL-bz-1,30MTL-bz-2,30MTL-zz-1,30MTL-zz-2,30MTL-zb-1-OUT_BB,30MTL-zb-1-IN_BB,30MTL-zb-2-OUT_BB,30MTL-zb-2-IN_BB"
      ,
      twinReverseBeamType: ""
      ,
      updateBy: "13647215045"
      ,
      updateTime: "2026-01-16 09:39:47"
    }

  ]

  return {
    MENU_DATA,
    MENU_LEVEL_FOUR
  }
})
