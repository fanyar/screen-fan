<template>
  <div class="h-full overflow-y-auto bg-[#071527] p-[20px] text-[14px] text-[#d8e9ff] scrollBar">
    <div class="mb-[20px] flex items-center justify-between">
      <div>
        <div class="text-[24px] font-semibold leading-[32px] text-white">预览组件</div>
        <div class="mt-[6px] text-[14px] leading-[22px] text-[#8ea8c7]">组件、主题、边框与图表预览</div>
      </div>
      <div class="flex items-center gap-[10px]">
        <el-button class="!h-[32px] !px-[12px] !text-[14px]" @click="setProperty('green')">绿色</el-button>
        <el-button class="!h-[32px] !px-[12px] !text-[14px]" @click="setProperty('cyan')">红色</el-button>
        <el-button class="!h-[32px] !px-[12px] !text-[14px]" @click="setProperty('orange')">橙色</el-button>
        <el-color-picker v-model="themeColor" @change="handleChangeTheme"></el-color-picker>
      </div>
    </div>

    <div class="grid grid-cols-[repeat(12,minmax(0,1fr))] gap-[20px]">
      <section class="col-span-3 h-[220px] overflow-hidden rounded-[6px] bg-[#0d2138] p-[16px]">
        <double-card height="100%" width="100%">
          <div class="flex h-full items-center justify-center text-[18px] font-medium text-white">柱状图</div>
        </double-card>
      </section>

      <section class="col-span-3 h-[220px] rounded-[6px] border border-[#1f4668] bg-[#0d2138] p-[16px]">
        <div class="mb-[12px] text-[18px] font-medium leading-[28px] text-white">动效组件</div>
        <div class="animate__animated animate__bounce rounded-[4px] bg-[#102c48] px-[14px] py-[12px] text-[14px] leading-[22px] text-[#9ed8ff]">
          An animated element
        </div>
      </section>

      <section class="col-span-3 h-[220px] rounded-[6px] border border-[#1f4668] bg-[#0d2138] p-[16px]">
        <div class="mb-[12px] text-[18px] font-medium leading-[28px] text-white">边框容器</div>
        <div class="card-container-border h-[132px]"><span>title</span></div>
      </section>

      <section class="col-span-3 h-[220px] rounded-[6px] border border-[#1f4668] bg-[#0d2138] p-[16px]">
        <div class="mb-[12px] text-[18px] font-medium leading-[28px] text-white">拖拽组件</div>
        <div class="relative h-[132px] rounded-[4px] border border-dashed border-[#315a7c] bg-[#09192a]">
          <Vue3DraggableResizable :parent="true">
            Hello World
          </Vue3DraggableResizable>
        </div>
      </section>

      <section class="col-span-4 h-[360px] overflow-hidden rounded-[6px] bg-[#0b1c31]">
        <BorderBox11 title="bar1" :color="['#2cd5ff', '#46e081']" style="height: 100%">
          <div class="sx-box-content">
            <bar1></bar1>
          </div>
        </BorderBox11>
      </section>

      <section class="col-span-4 h-[360px] overflow-hidden rounded-[6px] bg-[#0b1c31]">
        <BorderBox11 title="bar2" style="height: 100%">
          <div class="sx-box-content">
            <bar2></bar2>
          </div>
        </BorderBox11>
      </section>

      <section class="col-span-4 h-[360px] overflow-hidden rounded-[6px] bg-[#0b1c31]">
        <BorderBox11 title="bar3" style="height: 100%">
          <div class="sx-box-content">
            <bar3></bar3>
          </div>
        </BorderBox11>
      </section>

      <section class="col-span-4 h-[360px] overflow-hidden rounded-[6px] bg-[#0b1c31]">
        <BorderBox11 title="bar4" style="height: 100%">
          <div class="sx-box-content">
            <bar4></bar4>
          </div>
        </BorderBox11>
      </section>

      <section class="col-span-8 h-[360px] rounded-[6px] border border-[#1f4668] bg-[#0d2138] p-[16px]">
        <div class="mb-[12px] text-[18px] font-medium leading-[28px] text-white">主题预览</div>
        <div class="theme-demo mb-[16px] h-[72px] rounded-[4px] px-[20px] text-[18px] font-medium leading-[72px]">
          当前主题背景
        </div>
        <div class="mb-[16px] flex items-center gap-[12px]">
          <el-button class="!h-[32px] !px-[12px] !text-[14px]" type="primary">默认按钮</el-button>
          <Button border="Border5" color="#e18a3b">智慧医疗</Button>
          <Button border="Border5">智慧</Button>
        </div>
        <a href="javascript:;" class="theme-image block h-[132px] rounded-[4px] px-[20px] text-[14px] leading-[132px] text-white">
          换肤背景图
        </a>
      </section>
    </div>
  </div>
</template>

<script setup>
import doubleCard from '@/components/cardBox/doubleCard.vue'
import Bar1 from "./bigScreen/echarts/bar1.vue";
import Bar2 from "./bigScreen/echarts/bar2.vue";
import Bar3 from "./bigScreen/echarts/bar3.vue";
import Bar4 from "./bigScreen/echarts/bar4.vue";
import Vue3DraggableResizable from 'vue3-draggable-resizable'
import 'vue3-draggable-resizable/dist/Vue3DraggableResizable.css'
import 'animate.css';
import {BorderBox11, Button} from '@kjgl77/datav-vue3'
import {ref, onMounted} from "vue"
import {setStore, getStore} from "@/util/store"

let themeColor = ref('');

const setProperty = (type) => {
  document.documentElement.setAttribute('theme-mode', type)
  setStore({
    name: "theme-mode",
    content: type
  })
}

const handleChangeTheme = (color) => {
  document.documentElement.style.setProperty('--el-color-primary', color);
}

onMounted(() => {
  let type = getStore({name: "theme-mode"});
  if (type) {
    setProperty(type)
  }
})

</script>

<style scoped lang="scss">
.theme-demo {
  background: var(--tarin-base-background);
  color: var(--train-base-color);
}

.theme-image {
  background: var(--train-backImg) no-repeat left top;
  background-size: 100% 100%;
}

:deep(.dv-button-wrapper) {
  .dv-button-svg-line {
    stroke: var(--tarin-base-background)
  }
}
</style>
