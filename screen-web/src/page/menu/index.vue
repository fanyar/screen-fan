<template>
  <el-menu
      class="sx-side-menu h-full w-[180px] shrink-0 border-r-0 text-[16px]"
      :default-active="route.path"
  >
    <template v-for="item in userStore.MENU_LIST" :key="item.path || item.name">
      <template v-if="!item.children">
        <el-menu-item
            class="menu-item text-[14px]"
            :index="item.path"
            @click="openPage(item)"
        >
          {{ item.name }}
        </el-menu-item>
      </template>
      <template v-else>
        <el-sub-menu :index="item.name">
          <template #title>
            <span class="menu-title flex w-full items-center justify-between text-[14px]">
              <span class="truncate">{{ item.name }}</span>
              <el-icon class="menu-arrow ml-[8px] size-[14px] shrink-0 transition-transform duration-200">
                <ArrowDown/>
              </el-icon>
            </span>
          </template>
          <el-menu-item
              v-for="opt in item.children"
              :key="opt.path || opt.name"
              class="menu-item menu-item-child text-[14px]"
              :index="opt.path"
              @click="openPage(opt)"
          >
            {{ opt.name }}
          </el-menu-item>
        </el-sub-menu>
      </template>
    </template>
  </el-menu>
</template>

<script setup>
import {useRoute, useRouter} from "vue-router"
import {useUserStore} from "@/store"

let route = useRoute();
let router = useRouter();
let userStore = useUserStore();

const openPage = item => {
  router.push({
    path: item.path
  })
}
</script>

<style scoped lang="scss">
.sx-side-menu {
  background: var(--tarin-base-background);
  box-sizing: border-box;
  display: block;
  position: relative;
  overflow-x: hidden;
  overflow-y: auto;
  border: none;

  :deep(.el-menu) {
    background-color: transparent;
  }

  :deep(.el-sub-menu) {
    font-size: inherit;
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    box-sizing: border-box;
    height: 0.444444rem;
    padding: 0 0.185185rem !important;
    color: var(--train-base-color);
    font-size: inherit;
    line-height: 0.444444rem;
  }

  :deep(.el-menu-item) {
    background-color: transparent;
  }

  :deep(.el-menu-item.is-active) {
    background-color: var(--el-menu-hover-bg-color);
    color: var(--train-base-color);
  }

  :deep(.el-menu-item:hover),
  :deep(.el-sub-menu__title:hover) {
    background-color: rgb(255 255 255 / 12%);
  }

  :deep(.el-sub-menu__icon-arrow) {
    display: none;
  }

  :deep(.menu-arrow) {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 0.12963rem;
    height: 0.12963rem;
    font-size: 0.12963rem;
    line-height: 1;
  }

  :deep(.el-sub-menu.is-opened .menu-arrow) {
    transform: rotate(180deg);
  }

  :deep(.menu-item-child) {
    height: 0.407407rem;
    padding-left: 0.314815rem !important;
    font-size: inherit;
    line-height: 0.407407rem;
  }
}
</style>
