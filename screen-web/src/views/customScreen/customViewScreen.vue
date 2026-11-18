<template>
  <!-- 预览 -->
  <el-container>
    <el-main>
      <div class="view-module">
        <div class="module-item animate__animated"
             v-for="item in moduleList"
             :class="setViewModuleClass(item)"
             :style="setViewModule(item)">
          <component :is="item.moduleName"></component>
        </div>
      </div>
    </el-main>
  </el-container>
</template>

<script>
import 'animate.css';

import Bar1 from "@/components/bigScreen/echarts/bar1.vue"
import Bar2 from "@/components/bigScreen/echarts/bar2.vue"
import Bar3 from "@/components/bigScreen/echarts/bar3.vue"
import Bar4 from "@/components/bigScreen/echarts/bar4.vue"

export default {
  name: "customViewScreen",
  components: {
    Bar1, Bar2, Bar3, Bar4
  },
  data() {
    return {
      ifHaveVedioModel: false,
      vedioUrl: '',
      modelType: '',
      customScreenInfo: {},
      setupLegendFlag: false,
      bridgeLegendFlag: false,
      moduleItemW: 300,   //12宫格 某一宫格的宽度
      moduleItemH: 220,
      moduleList: [
        /*{ moduleId: "10000", index: 0, left: 150, top: 110, moduleName: "OverallProgress", name: "总体进度信息",seatPos: "0", seizeSeat: "1 x 1", x: 1, y: 1 },
        { moduleId: "10001", index: 4, left: 150, top: 330, moduleName: "TypeProduction", name: "各类型生产情况",seatPos: "4", seizeSeat: "1 x 1", x: 1, y: 1 },
        { moduleId: "10002", index: 8, left: 150, top: 550, moduleName: "BridgeProgress", name: "各桥梁预制进度",seatPos: "8", seizeSeat: "1 x 1", x: 1, y: 1 },
        { moduleId: "10005", index: 3, left: 1050, top: 110, moduleName: "ProcessQualityInfo", name: "工序质检及任务信息",seatPos: "3", seizeSeat: "1 x 1", x: 1, y: 1 },
        { moduleId: "10006", index: 7, left: 1050, top: 330, moduleName: "ProductionResourceInfo", name: "生产资源信息",seatPos: "7,11", seizeSeat: "1 x 2", x: 1, y: 2 },

        { moduleId: "10023", index: 1, left: 450, top: 110, moduleName: "BridgeErectingMachineDetails", name: "架桥机详情",seatPos: "1，5", seizeSeat: "1 x 2", x: 1, y: 2 },
        // { index: 1, left: 225, top: 75, moduleName: "OverviewTansportation", name: "运梁总览",seatPos: "1", seizeSeat: "1 x 1", x: 1, y: 1 },
        { moduleId: "10015",  index: 2, left: 750, top: 110, moduleName: "PlanTansportation", name: "运梁计划对比",seatPos: "2", seizeSeat: "1 x 1", x: 1, y: 1 },
        // { index: 5, left: 225, top: 225, moduleName: "ToolTansportation", name: "运梁工具信息",seatPos: "5", seizeSeat: "1 x 1", x: 1, y: 1 },
        { moduleId: "10019",  index: 6, left: 750, top: 330, moduleName: "FieldNotesBeam", name: "现场存梁记录",seatPos: "6", seizeSeat: "1 x 1", x: 1, y: 1 },

        { moduleId: "10003",  index: 9, left: 450, top: 550, moduleName: "ProductProgressCompare", name: "生产进度对照",seatPos: "9，10", seizeSeat: "2 x 1", x: 2, y: 1 },
        // { index: 9, left: 225, top: 375, moduleName: "OverallInstallation", name: "总体架设情况",seatPos: "9", seizeSeat: "1 x 1", x: 1, y: 1 },
        // { index: 10, left: 375, top: 375, moduleName: "BridgeErectingMachine", name: "架桥机情况",seatPos: "10", seizeSeat: "1 x 1", x: 1, y: 1 },
      */
      ]

    }
  },
  watch: {
    '$store.state.screen.customScreen': {
      deep: true,
      immediate: true,
      handler(n) {
        if (this.$route.path == '/test/screen/customView') {
          if (n['moduleListView'] && n['moduleListView'].length) {
            this.moduleList = n['moduleListView'];

            this.setupLegendFlag = this.moduleList.findIndex(opt => opt.moduleId == '10021') > -1;
            this.bridgeLegendFlag = this.moduleList.findIndex(opt => opt.moduleId == '10007') > -1;
            this.customScreenInfo['hasBackground'] = n.formScreen.hasBackground;
          }
        }
      }
    }
  },
  created() {

  },
  methods: {
    setViewModule(data) {
      // 1X1  width: 25%; hieght: 29.2rem
      let w = 25 * data.x + '%';
      let style = {
        'width': `calc(${w} - 2rem)`,
        'height': 29.2 * data.y + 'rem',
        'top': 29.2 * (data.top - this.moduleItemH / 2) / this.moduleItemH + 'rem',
        'left': 25 * (data.left - this.moduleItemW / 2) / this.moduleItemW + '%',
      }
      if (data.index == 4 || data.index == 5 || data.index == 6 || data.index == 7) {
        style['top'] = parseFloat(style['top']) + 2 + 'rem'
      } else if (data.index == 8 || data.index == 9 || data.index == 10 || data.index == 11) {
        style['top'] = parseFloat(style['top']) + 4 + 'rem'
      }
      if (data.index == 3 || data.index == 7 || data.index == 11) {
        style['width'] = `calc(${w})`
      }
      return style;
    },
    setViewModuleClass(data) {
      switch (data.index) {
        case 0:
        case 4:
        case 8:
          return {
            'animate__fadeInLeft': true,
            'pointAll': true,
          }
          break;
        case 3:
        case 7:
        case 11:
          return {
            'animate__fadeInRight': true,
            'pointAll': true,
          }
          break;
        default:
          return {
            'animate__fadeIn': true,
            'pointNone': true,
          }
          break;
      }
    }
  }
}
</script>

<style scoped lang="scss">
.view-module {
  position: relative;
  width: 100%;
  height: 100%;

  .module-item {
    position: absolute;
  }
}
</style>
