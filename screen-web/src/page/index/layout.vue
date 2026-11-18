<template>
  <div class="flex h-full bg-[#f0f2f5] text-[14px] text-[#333]">
    <menuBox></menuBox>
    <div class="flex min-w-0 flex-1 flex-col">
      <div class="flex h-[56px] shrink-0 items-center justify-end bg-[var(--tarin-base-background)] px-[20px]">
        <el-dropdown popper-class="user-info" @command="handleCommand">
          <span class="flex cursor-pointer items-center text-[14px] text-[var(--train-base-color)] outline-none">
            {{ userInfo.name }}
            <el-icon class="ml-[6px] size-[14px]">
              <arrow-down/>
            </el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <el-container class="min-h-0 flex-1">
        <el-main class="min-h-0 p-[20px] text-[14px]">
          <router-view></router-view>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import menuBox from '../menu/index.vue'
import {getStore} from "@/util/store"
import {useRouter} from "vue-router"
import {useUserStore} from "@/store/modules/user.js"

let router = useRouter();
let userStore = useUserStore();
let userInfo = getStore({name: "userInfo"}) || { name: 'admin' };

const handleCommand = (command) => {
  if (command == "logout") {
    router.push({path: '/test/login'});
    userStore.logout();
  }
}
</script>

<style lang="scss">
.user-info.el-popper.is-light {
  .el-scrollbar {
    .el-dropdown-menu {
      .el-dropdown-menu__item {
        color: var(--tarin-base-background);
      }
    }
  }
}
</style>
