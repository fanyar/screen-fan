<template>
  <div id="menu-scale-wrapper" style="background-color: rgba(0,0,0,0.6)">
    <div class="menu-box">
      <!-- 触发区域 -->
      <div style="width: 0px; height: 0px; pointer-events: auto; position: relative;">
        <!--  最底层：中心背景 bg1（不参与交互） -->
        <div class="menu-ring-bg1"/>

        <!--  顶层：点击按钮（可交互） -->
        <div
            @click=" toggleMenu
        "
            class="home-img-box"
            :class="isOpen ? 'rotate-90' : 'rotate-0'"
        >
          <img
              :src="getImageUrl('menu/logo.png')"
              class="home-img"
          />
        </div>

        <!--  所有转盘（整体抬高，压住背景） -->
        <div class="zhuanpan">
          <!-- 一级菜单 -->
          <MenuRing
              ref="l1MenuRingRef"
              :items="basicStore.MENU_DATA"
              :radius="380"
              :level="2"
              :is-active="l1ActiveDelayed"
              :should-handle-keyboard="false"
              @item-select="handleL1Select"
              @item-right-db-click="handleL1RightDbClick"
              @item-right-triple-click="handleL1RightTripleClick"
              :z-index="20"
              :rotation-offset="9"
              :center-offset-x="288"
              :styleOpt="styleOpt1"
              :verticalSpacing="130"
          />

          <!-- 二级菜单 -->
          <MenuRing
              ref="l2MenuRingRef"
              :items="l2Items"
              :radius="605"
              :level="3"
              :is-active="l2ActiveDelayed"
              :should-handle-keyboard="false"
              :reset-rotation="resetRotation"
              @item-select="handleL2Select"
              @item-right-db-click="handleL2RightDbClick"
              @item-right-triple-click="handleL2RightTripleClick"
              :z-index="15"
              :rotation-offset="rotationOffset"
              :center-offset-x="400"
              :styleOpt="styleOpt2"
          />

          <!-- 三级菜单（level 4）：仅 m4-3 / m4-4 显示 -->
          <Level4Menu v-if="showLevel4" :is-active="showLevel4 && l3ActiveDelayed" :reset-rotation="resetRotation"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, ref, watch, onMounted, onUnmounted} from 'vue';
import {storeToRefs} from 'pinia';
import {useRouter, useRoute} from 'vue-router';
import MenuRing from './MenuRing.vue';
import Level4Menu from './Level4Menu.vue';
import {useBasicStore, useMenuStore,} from '@/store';

import {
  getImageUrl,
  updateMenuItemActive,
  findMenuItemByRoute,
  resetAllL2MenuActive,
  resetAllL1MenuActive
} from '@/util';

const router = useRouter();
const route = useRoute();
const menuStore = useMenuStore();
const basicStore = useBasicStore();
// 使用 storeToRefs 保持响应式
const {isOpen, activeL1, activeL2} = storeToRefs(menuStore);

// 一级、二级、三级菜单的延迟激活状态（用于递进效果）
const l1ActiveDelayed = ref(false);
const l2ActiveDelayed = ref(false);
const l3ActiveDelayed = ref(false);
let l1Timer = null;
let l2Timer = null;
let l3Timer = null;

// 切换菜单
let lastToggleTs = 0;
const toggleMenu = () => {
  const now = Date.now();
  if (now - lastToggleTs < 500) return;
  lastToggleTs = now;
  menuStore.toggleMenu();
};

const styleOpt2 = {
  width: 1300,
  height: 1300,
  offsetX: -750,
};

const styleOpt3 = {
  width: 1600,
  height: 1600,
  offsetX: -900,
};

const styleOpt1 = {
  width: 650,
  height: 650,
  offsetX: -480,
};

// 仅当二级菜单为 T梁生产线(m4-3) 或 π梁生产线(m4-4) 时显示 level4
const showLevel4 = computed(() => {
  return activeL2.value === 'm4-3' || activeL2.value === 'm4-4';
});

// 二级菜单的数据计算
const selectedL1Item = computed(() => {
  return basicStore.MENU_DATA.find(i => i.id === activeL1.value);
});

// 用于触发子组件重置旋转角度
const resetRotation = ref(false);

// 更新菜单项的 active 状态
const updateMenuActive = (menuId, isActive) => {
  updateMenuItemActive(basicStore.MENU_DATA, menuId, isActive);
};

// 监听菜单展开/收起状态，实现递进效果
watch(
    () => isOpen.value,
    newVal => {
      // 统一递进：一级立即，二级稍延迟，三级再延迟；收起时先三级、二级后一级
      if (l1Timer) {
        clearTimeout(l1Timer);
        l1Timer = null;
      }
      if (l2Timer) {
        clearTimeout(l2Timer);
        l2Timer = null;
      }
      if (l3Timer) {
        clearTimeout(l3Timer);
        l3Timer = null;
      }

      if (newVal) {
        // 展开：一级立即出现，二级稍晚一点出现，三级再晚一点
        l1ActiveDelayed.value = true;
        if (activeL1.value) {
          l2Timer = setTimeout(() => {
            l2ActiveDelayed.value = true;
          }, 160); // 展开二级略慢，强化“递进”
          l3Timer = setTimeout(() => {
            l3ActiveDelayed.value = true;
          }, 320);
        } else {
          l2ActiveDelayed.value = false;
          l3ActiveDelayed.value = false;
        }
      } else {
        // 收起：三级、二级立即收起，一级再慢一点收起
        l2ActiveDelayed.value = false;
        l3ActiveDelayed.value = false;
        l1Timer = setTimeout(() => {
          l1ActiveDelayed.value = false;
        }, 200); // 一级收起比二级慢，产生层级感
      }
    },
    {immediate: true}
);

// 当一级菜单切换时，重置二级菜单的旋转角度并更新 active 状态
watch(activeL1, (newVal, oldVal) => {
  // 仅在一级切换时重置旋转
  if (newVal !== oldVal) {
    resetRotation.value = !resetRotation.value;
  }

  // 更新一级菜单的 active 状态
  if (oldVal) {
    updateMenuActive(oldVal, false);
  }
  if (newVal) {
    updateMenuActive(newVal, true);
    // 如果菜单是打开的，延迟显示二级菜单，保留递进效果
    if (isOpen.value) {
      if (l2Timer) {
        clearTimeout(l2Timer);
      }
      // 切换一级不再先收起二级，避免卡顿
      l2Timer = setTimeout(() => {
        l2ActiveDelayed.value = true;
      }, 120);
    }
  } else {
    // 如果 newVal 为 null，说明取消选中，所有一级菜单的 active 都应该为 false
    resetAllL1MenuActive(basicStore.MENU_DATA);
    l2ActiveDelayed.value = false; // 立即隐藏二级菜单
  }

  // 当一级菜单切换时，重置所有二级菜单的 active 状态
  if (newVal !== oldVal) {
    resetAllL2MenuActive(basicStore.MENU_DATA);
  }
});

// 监听二级菜单选中状态变化，更新 active 状态
watch(activeL2, (newVal, oldVal) => {
  if (oldVal) {
    updateMenuActive(oldVal, false);
  }
  if (newVal) {
    updateMenuActive(newVal, true);
    // 如果选中了 m4-3 或 m4-4，且菜单已打开，立即激活 l3ActiveDelayed（不需要延迟）
    if ((newVal === 'm4-3' || newVal === 'm4-4') && isOpen.value) {
      if (l3Timer) {
        clearTimeout(l3Timer);
        l3Timer = null;
      }
      // 如果 l2ActiveDelayed 已经激活，立即激活 l3，否则等待 l2 激活后再激活
      if (l2ActiveDelayed.value) {
        l3ActiveDelayed.value = true;
      } else {
        // 等待 l2 激活后再激活 l3
        l3Timer = setTimeout(() => {
          l3ActiveDelayed.value = true;
        }, 160); // 与 l2 的延迟时间相同
      }
    } else {
      // 如果不是 m4-3 或 m4-4，隐藏 l3
      l3ActiveDelayed.value = false;
      if (l3Timer) {
        clearTimeout(l3Timer);
        l3Timer = null;
      }
    }
  } else {
    // 如果 newVal 为 null，说明取消选中，所有二级菜单的 active 都应该为 false
    resetAllL2MenuActive(basicStore.MENU_DATA);
    l3ActiveDelayed.value = false;
    if (l3Timer) {
      clearTimeout(l3Timer);
      l3Timer = null;
    }
  }
});

// ---------- 视角相关逻辑 ----------

// 统一的视角 ID 生成
const getViewId = (level, item) => {
  // if (level === 1) {
  return item.id;
  // }
  // const parent = selectedL1Item.value;
  // return parent && parent.id ? `${parent.id}-${item.id}` : `${item.id}`;
};


// 统一的菜单项处理函数（仅用于一二级菜单）
const createMenuHandlers = level => {
  const getId = item => getViewId(level, item);

  return {
    // 左键点击
    onSelect: item => {
      if (level === 1) {
        menuStore.handleL1Select(item);
      } else {
        menuStore.handleL2Select(item);
        if (item.route) {
          // router.push({path: item.route});
        }
      }
    },
    // 右键双击
    onRightDbClick: item => {

    },
    // 右键三击
    onRightTripleClick: item => {

    },
  };
};

// 一级菜单处理器
const l1Handlers = createMenuHandlers(1);
const handleL1Select = l1Handlers.onSelect;
const handleL1RightDbClick = l1Handlers.onRightDbClick;
const handleL1RightTripleClick = l1Handlers.onRightTripleClick;

// 二级菜单处理器
const l2Handlers = createMenuHandlers(2);
const handleL2Select = l2Handlers.onSelect;
const handleL2RightDbClick = l2Handlers.onRightDbClick;
const handleL2RightTripleClick = l2Handlers.onRightTripleClick;

const l2Items = computed(() => {
  console.log(selectedL1Item.value?.children)
  return selectedL1Item.value?.children || [];
  // basicStore.MENU_DATA[2].children;
});

const rotationOffset = computed(() => {
  return l2Items.value.length > 6 ? (l2Items.value.length - 6) * 8 : 0;
});

// MenuRing 组件引用
const l1MenuRingRef = ref(null);
const l2MenuRingRef = ref(null);

// 根据路由恢复菜单选中状态
const restoreMenuSelectionByRoute = () => {
  const currentPath = route.path;

  // 如果当前已经有选中的二级菜单，且该菜单项的路由匹配当前路由，则保持选中状态
  if (activeL2.value) {
    const currentL2Item = selectedL1Item.value?.children?.find(child => child.id === activeL2.value);
    if (currentL2Item && currentL2Item.route && currentPath.startsWith(currentL2Item.route)) {
      // 当前选中的菜单项匹配路由，不需要恢复
      return;
    }
  }

  const menuInfo = findMenuItemByRoute(basicStore.MENU_DATA, currentPath);

  if (menuInfo) {
    // 设置菜单选中状态（watch 会自动更新 active 属性）
    menuStore.activeL1 = menuInfo.l1Id || null;
    menuStore.activeL2 = menuInfo.l2Id || null;
  } else {
    // 如果路由不匹配任何菜单项，清空选中状态
    menuStore.activeL1 = null;
    menuStore.activeL2 = null;
  }
};

// 监听路由变化，更新菜单选中状态
watch(
    () => route.path,
    () => {
      restoreMenuSelectionByRoute();
    }
);

// 键盘控制：处理 ←/→ 和 A/D 键的层级切换
let lastKeyPressTime = 0;
const KEY_THROTTLE_MS = 200; // 节流时间：200ms

// 键盘控制：处理 W/S/↑/↓ 键的菜单项选中切换
let lastMenuNavKeyPressTime = 0;
const MENU_NAV_KEY_THROTTLE_MS = 200; // 节流时间：200ms
// 2级菜单切换计数器：动态计算旋转参数
let l2MenuSwitchCount = 0;
let l2LastDirection = null; // 记录上一次按键方向

// 根据1级菜单ID获取2级菜单的旋转配置
const getL2RotateConfig = l1MenuId => {
  switch (l1MenuId) {
    case 'm1': // 项目介绍 (6个子菜单)
      return {
        rotateAfterSwitches: 6,
        rotateItems: 0,
      };
    case 'm2': // 智慧化决策 (7个子菜单)
      return {
        rotateAfterSwitches: 1,
        rotateItems: -1,
      };
    case 'm3': // 数字化管理 (8个子菜单)
      return {
        rotateAfterSwitches: 1,
        rotateItems: -1,
      };
    case 'm4': // 工业化生产 (8个子菜单)
      return {
        rotateAfterSwitches: 1,
        rotateItems: -1,
      };
    case 'm5': // 系统设置 (2个子菜单)
      return {
        rotateAfterSwitches: 2,
        rotateItems: 0,
      };
    default:
      return {
        rotateAfterSwitches: 1,
        rotateItems: -1,
      };
  }
};

const handleMenuNavigationKeyDown = e => {
  // 只在菜单打开时响应
  if (!isOpen.value) return;
  // 三级菜单“已进入交互态”（已有选中项）时，W/S/↑/↓ 才交给 Level4Menu 处理，避免二级也跟着动
  // 否则（仅显示三级但未选中任何三级项，比如按了一次 A 取消三级选中），W/S 应该回到二级菜单

  const isUpKey = e.key === 'w' || e.key === 'W' || e.key === 'ArrowUp';
  const isDownKey = e.key === 's' || e.key === 'S' || e.key === 'ArrowDown';

  if (!isUpKey && !isDownKey) return;

  // 节流控制
  const now = Date.now();
  if (now - lastMenuNavKeyPressTime < MENU_NAV_KEY_THROTTLE_MS) return;
  lastMenuNavKeyPressTime = now;

  // 阻止默认行为，阻止事件传播到子组件
  e.preventDefault();
  e.stopPropagation();

  // 判断当前层级并切换菜单项
  if (activeL1.value && !activeL2.value) {
    // 切换1级菜单
    const items = basicStore.MENU_DATA;
    if (!items || items.length === 0) return;

    const currentIndex = items.findIndex(item => item.id === activeL1.value);
    if (currentIndex === -1) return;

    // 计算下一个索引（循环）
    const newIndex = isUpKey ? (currentIndex - 1 + items.length) % items.length : (currentIndex + 1) % items.length;
    const newItem = items[newIndex];

    if (newItem) {
      // 切换菜单项选中状态
      menuStore.handleL1Select(newItem);

      // 每次切换立即旋转：
      if (l1MenuRingRef.value) {
        const rotateDirection = isUpKey ? 'down' : 'up';
        l1MenuRingRef.value.rotateByItems(1, rotateDirection);
      }
    }
  } else if (activeL1.value && activeL2.value) {
    // 切换2级菜单
    const items = selectedL1Item.value?.children || [];
    if (!items || items.length === 0) return;

    // 根据1级菜单ID获取旋转配置
    const rotateConfig = getL2RotateConfig(activeL1.value);

    const currentIndex = items.findIndex(item => item.id === activeL2.value);
    if (currentIndex === -1) return;

    // 计算下一个索引（循环）
    const newIndex = isUpKey ? (currentIndex - 1 + items.length) % items.length : (currentIndex + 1) % items.length;
    const newItem = items[newIndex];

    if (newItem) {
      // 只切换菜单项选中状态，不立即旋转（会自动处理路由跳转和视角切换）
      handleL2Select(newItem);

      // 判断按键方向
      const currentDirection = isUpKey ? 'up' : 'down';

      // 如果方向改变，重置计数器（连续相同方向才计数）
      if (l2LastDirection !== null && l2LastDirection !== currentDirection) {
        l2MenuSwitchCount = 0;
      }

      // 更新方向并增加切换计数
      l2LastDirection = currentDirection;
      l2MenuSwitchCount++;

      // 使用动态配置：连续按指定次数后，统一旋转指定项的角度
      if (l2MenuSwitchCount >= rotateConfig.rotateAfterSwitches) {
        l2MenuSwitchCount = 0; // 重置计数器
        // 统一旋转2级菜单指定项的角度
        if (l2MenuRingRef.value) {
          l2MenuRingRef.value.rotateByItems(rotateConfig.rotateItems, currentDirection);
        }
      }
    }
  }
};

const handleKeyDown = e => {
  // 只在菜单打开时响应
  if (!isOpen.value) return;

  // 只处理 ←/→ 方向键和 A/D 键
  const isLeftKey = e.key === 'ArrowLeft' || e.key === 'a' || e.key === 'A';
  const isRightKey = e.key === 'ArrowRight' || e.key === 'd' || e.key === 'D';

  if (!isLeftKey && !isRightKey) return;

  // 节流控制
  const now = Date.now();
  if (now - lastKeyPressTime < KEY_THROTTLE_MS) return;
  lastKeyPressTime = now;

  // 阻止默认行为
  e.preventDefault();
  e.stopPropagation();

  // ← 键或 A 键：返回上一级
  if (isLeftKey) {
    // 如果当前在三级菜单（showLevel4 为 true）：
    // - 第一次：仅取消三级菜单选中
    // - 第二次：再清空 activeL2，退出三级返回二级
    if (showLevel4.value) {

      menuStore.activeL2 = null;
      return;
    }
    // 如果当前在二级菜单（activeL2 有值），清空 activeL2，保持 activeL1，允许重新选择一级菜单
    if (activeL2.value) {
      menuStore.activeL2 = null;
      return;
    }
    // 如果当前在一级菜单，不做任何操作（已经是最上级）
  }

  // → 键或 D 键：进入下一级
  if (isRightKey) {
    // 如果当前在二级菜单（activeL2 有值），且支持三级菜单（m4-3 或 m4-4），进入三级菜单
    // 注意：三级菜单的显示是自动的（通过 showLevel4），这里不需要额外操作
    // 但如果当前选中的二级菜单项不是 m4-3 或 m4-4，则无法进入三级菜单
    if (activeL2.value) {
      // 已经在二级菜单：
      // - 如果支持三级菜单，showLevel4 会自动显示
      // - 但“进入三级交互态”需要有默认选中项，否则 Level4Menu 不会因为按 D/→ 自动选中第一条

      return;
    }
    // 如果当前在一级菜单（activeL1 有值且 activeL2 为空），且当前选中的一级菜单项有子项，自动进入二级菜单
    if (activeL1.value && !activeL2.value) {
      const currentL1Item = selectedL1Item.value;
      if (currentL1Item && currentL1Item.children && currentL1Item.children.length > 0) {
        // 自动选择第一个子项，进入二级菜单
        const firstChild = currentL1Item.children[0];
        menuStore.handleL2Select(firstChild);
        // 如果有路由，自动跳转
        if (firstChild.route) {
          router.push({path: firstChild.route});
        }
        // 切换视角
        const getId = item => getViewId(2, item);
      }
    }
  }
};

// 组件挂载时恢复菜单选中状态并添加键盘事件监听
onMounted(() => {
  restoreMenuSelectionByRoute();
  // 添加键盘事件监听（层级切换和菜单项切换）
  window.addEventListener('keydown', handleKeyDown);
  window.addEventListener('keydown', handleMenuNavigationKeyDown);
});

// 组件卸载时清理定时器和移除键盘事件监听
onUnmounted(() => {
  if (l1Timer) {
    clearTimeout(l1Timer);
    l1Timer = null;
  }
  if (l2Timer) {
    clearTimeout(l2Timer);
    l2Timer = null;
  }
  // 移除键盘事件监听
  window.removeEventListener('keydown', handleKeyDown);
  window.removeEventListener('keydown', handleMenuNavigationKeyDown);
});
</script>

<style scoped>
.menu-ring-bg1 {
  position: absolute;
  width: 156px;
  height: 360px;
  pointer-events: none;
  background-image: url('@/assets/images/menu/menu-ring-bg1.png');
  background-repeat: no-repeat;
  background-size: 100%;
  background-position: bottom;
  z-index: 100;
  left: 8px;
  top: -73px;
}

.menu-ring-bg1::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 50%;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  transform: translate(-377px, -80%);
  pointer-events: none;
  backdrop-filter: blur(3px);
  /* background: radial-gradient(circle, rgba(220, 214, 252, 0.2), rgba(220, 214, 252, 0)); */
}

#menu-scale-wrapper {
  position: fixed;
  top: 0;
  width: 1920px;
  height: 1080px;
  pointer-events: none;
}

.zhuanpan {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 0;
  height: 0;
  position: absolute;
  left: 0;
  top: 0;
  z-index: 20;

}

.home-img-box {
  position: absolute;
  left: -3px;
  top: 50%;
  z-index: 100;
  height: 32px;
  width: 32px;
  transform: translate(0, -50%) rotate(0deg) skewX(0) skewY(0) scaleX(1) scaleY(1);
  transition-property: transform;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 500ms;
}

.rotate-90 {
  transform: rotate(90deg);
}

.rotate-0 {
  transform: rotate(0deg);
}

.home-img {
  width: 30px;
  height: 30px;
}

.menu-box {
  display: flex;
  user-select: none;
  align-items: center;
  z-index: 50;
  left: 0.027778rem;
  top: 0px;
  bottom: 0px;
  position: fixed;
}
</style>
