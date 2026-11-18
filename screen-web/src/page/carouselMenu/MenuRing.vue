<template>
  <div
      class="menu-ring-box"
      ref="ringRef"
      :style="{
      zIndex,
      transform: isActive ? 'scale(1)' : 'scale(0.5)',
      opacity: isActive ? 1 : 0,
      pointerEvents: isActive ? 'auto' : 'none',
    }"
      @pointerdown="handlePointerDown"
      @pointermove="handlePointerMove"
      @pointerup="handlePointerUp"
      @pointercancel="handlePointerUp"
      @pointerleave="handlePointerUp"
  >
    <!-- 背景 -->
    <div
        class="pointer-events-none absolute left-[-30px] bg-cover bg-center"
        :class="['menu-ring-bg' + level]"
        style="z-index: 5"
    />

    <!-- 装饰虚线环 -->
    <div
        v-if="level == 2"
        class="levelBg"
        :style="{
        transform: `rotate(${rotation * -0.5 + rotationOffset}deg)`,
        zIndex: 0,
      }"
    />
    <div
        v-if="level == 3"
        class="levelBg"
        style="position: absolute; margin-left: -750px; width: 1303px; height: 1300px; border-radius: 9258px;
        transition-property: transform;transition-duration: 700ms;backdrop-filter: blur(3px);
        transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);"
        :style="{
        transform: `rotate(${rotation * -0.5 + rotationOffset}deg)`,
        zIndex: 0,
      }"
    />
    <!--  菜单项：level 2/3 默认样式 -->
    <template v-if="level !== 4">
      <div
          v-for="(item, index) in displayItems"
          :key="item.id"
          class="group absolute"
          style="position:absolute"
          :style="{
          transform: `translate3d(${getPosRem(getItemAngle(index), radius).x}, ${getPosRem(getItemAngle(index), radius).y}, 0)`,
          zIndex: 1000,
        }"
          @click="handleItemClick(item)"
          @contextmenu.prevent="handleItemRightClick($event, item)"
      >
        <div
            class="flex flex-col items-center transition-all duration-300"
            :class="item.active ? 'scale-110' : 'scale-100 hover:scale-105'"
            style="display: flex; flex-direction: column;align-items: center;transition-property: all;
            transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1); transition-duration: 300ms;"
        >
          <img :src="getMenuItemIcon(item)" alt="menu" class="h-[36px] w-[36px]" style="height: 36px; width: 36px"/>

          <div
              class="whitespace-nowrap px-[8px] pt-[10px] text-white transition-all duration-300"
              style="color: #fff; padding-left: 8px; padding-right: 8px; padding-top: 10px;
              white-space: nowrap;transition-property: all;transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);transition-duration: 300ms;"
              :style="{
              fontSize: item.active ? '16px' : '14px',
              textShadow: item.active
                ? '0 0 8px #8542FF, 0 0 12px #8542FF, 0 0 12px #8542FF, 0 0 12px #8542FF'
                : 'none',
            }"
          >
            {{ item.label }}
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, onUnmounted, watch} from 'vue';
import {pxToRem, getImageUrl} from '@/util';
import {useClick} from '@/hooks/useClick';

const props = defineProps({
  items: Array,
  radius: Number,
  isActive: Boolean,
  zIndex: Number,
  level: Number,
  verticalSpacing: {type: Number, default: 170},
  rotationOffset: {type: Number, default: 0},
  centerOffsetX: {type: Number, default: 0},
  styleOpt: {type: Object, default: () => ({})},
  resetRotation: Boolean,
  shouldHandleKeyboard: {type: Boolean, default: true}, // 是否应该响应键盘事件
});
const emit = defineEmits(['item-select', 'item-right-click', 'item-right-db-click', 'item-right-triple-click']);

const rotation = ref(0);
const ringRef = ref(null);
const isSnapAnimating = ref(false);
// 记录上一次各菜单项的激活状态，用于判断 false -> true 的变化
const prevActiveMap = ref({});
const dragState = ref({
  isDragging: false,
  currentRotation: 0,
  velocity: 0,
  lastTime: 0,
  lastY: 0,
});

let requestRef = null;

const MAX_ITEMS_BY_LEVEL = {2: 4, 3: 6};

// 键盘控制相关
let lastKeyPressTime = 0;
const KEY_THROTTLE_MS = 150; // 节流时间：150ms

const currentSpacing = computed(() => (props.verticalSpacing / props.radius) * (180 / Math.PI));
const defaultCount = computed(() => MAX_ITEMS_BY_LEVEL[props.level] || props.items.length);

const displayItems = computed(() => props.items);

// 获取菜单项图标（根据 active 状态动态拼接）
const getMenuItemIcon = item => {
  if (!item.icon) return '';
  const iconName = item.icon;
  const suffix = item.active ? '-active' : '';
  return getImageUrl(`menu/${iconName}${suffix}.png`);
};

//  总角度（无限循环核心）
const totalAngle = computed(() => displayItems.value.length * currentSpacing.value);

//  居中偏移
// const listCenter = computed(() => ((defaultCount.value - 1) * currentSpacing.value) / 2);
const listCenter = computed(() => {
  const res = displayItems.value.length > defaultCount.value ? displayItems.value.length : defaultCount.value;
  return ((res - 1) * currentSpacing.value) / 2;
});

//   无限循环角度计算
const getItemAngle = index => {
  const spacing = currentSpacing.value;
  const count = displayItems.value.length || 1;

  // 基础槽位角度
  let angle = index * spacing;
  angle += rotation.value + props.rotationOffset;

  const total = count * spacing;
  angle = ((angle % total) + total) % total;

  return angle - listCenter.value;
};

const getPosRem = (angleInDegrees, radius, offsetX = 0) => {
  const rad = (angleInDegrees * Math.PI) / 180;
  const x = Math.cos(rad) * radius - props.centerOffsetX + offsetX;
  const y = Math.sin(rad) * radius;
  return {
    x: x + 'px',//pxToRem(x),
    y: y + 'px'//pxToRem(y),
  };
};

// 惯性动画
const animate = () => {
  if (!dragState.value.isDragging) {
    if (Math.abs(dragState.value.velocity) > 0.01) {
      dragState.value.velocity *= 0.95;
      rotation.value += dragState.value.velocity;
      requestRef = requestAnimationFrame(animate);
    } else if (!isSnapAnimating.value) snapToGrid();
  }
};

//吸附
const snapToGrid = () => {
  const spacing = currentSpacing.value;
  isSnapAnimating.value = true;

  let target = Math.round(rotation.value / spacing) * spacing;
  const diff = target - rotation.value;

  if (Math.abs(diff) < 0.1) {
    rotation.value = target;
    isSnapAnimating.value = false;
    return;
  }

  rotation.value += diff * 0.15;
  requestRef = requestAnimationFrame(snapToGrid);
};

// 键盘控制：处理 W/S/↑/↓ 键旋转菜单
const handleKeyDown = e => {
  // 只在菜单激活时响应
  if (!props.isActive) return;

  // 如果 shouldHandleKeyboard 为 false，不响应键盘事件（由父组件统一处理）
  if (!props.shouldHandleKeyboard) return;

  // 只处理 W/S/↑/↓ 键，不处理 ←/→ 方向键
  const isUpKey = e.key === 'w' || e.key === 'W' || e.key === 'ArrowUp';
  const isDownKey = e.key === 's' || e.key === 'S' || e.key === 'ArrowDown';

  if (!isUpKey && !isDownKey) return;

  // 节流控制
  const now = Date.now();
  if (now - lastKeyPressTime < KEY_THROTTLE_MS) return;
  lastKeyPressTime = now;

  // 阻止默认行为
  e.preventDefault();
  e.stopPropagation();

  // 计算旋转步长（一个菜单项的角度）
  const spacing = currentSpacing.value;

  // W/↑：向上旋转（逆时针，减小 rotation）
  if (isUpKey) {
    rotation.value -= spacing;
  }
  // S/↓：向下旋转（顺时针，增加 rotation）
  else if (isDownKey) {
    rotation.value += spacing;
  }

  // 旋转后自动吸附到最近的菜单项位置
  if (requestRef) cancelAnimationFrame(requestRef);
  isSnapAnimating.value = false;
  snapToGrid();
};

// 拖拽
const handlePointerDown = e => {
  if (!props.isActive) return;
  e.stopPropagation();
  e.preventDefault();

  dragState.value.isDragging = true;
  dragState.value.currentRotation = rotation.value;
  dragState.value.velocity = 0;
  dragState.value.lastTime = performance.now();
  dragState.value.lastY = e.clientY;

  if (requestRef) cancelAnimationFrame(requestRef);
  isSnapAnimating.value = false;
};

const handlePointerMove = e => {
  if (!dragState.value.isDragging) return;

  const now = performance.now();
  const deltaY = e.clientY - dragState.value.lastY;
  const ROTATE_RATIO = 0.18;

  rotation.value += deltaY * ROTATE_RATIO;

  const dt = now - dragState.value.lastTime;
  if (dt > 0) dragState.value.velocity = deltaY * 0.15;

  dragState.value.lastTime = now;
  dragState.value.lastY = e.clientY;
};

const handlePointerUp = () => {
  if (!dragState.value.isDragging) return;
  dragState.value.isDragging = false;
  requestRef = requestAnimationFrame(animate);
};

// 重置
const animateToRotation = (target, duration = 350) => {
  const start = rotation.value;
  const diff = target - start;
  const startTime = performance.now();

  const easeOutCubic = t => 1 - Math.pow(1 - t, 3);

  const run = now => {
    const elapsed = now - startTime;
    const progress = Math.min(elapsed / duration, 1);
    const eased = easeOutCubic(progress);

    rotation.value = start + diff * eased;

    if (progress < 1) {
      requestRef = requestAnimationFrame(run);
    }
  };

  cancelAnimationFrame(requestRef);
  requestRef = requestAnimationFrame(run);
};

// 旋转到指定菜单项位置
const rotateToItem = targetIndex => {
  if (!props.isActive) return;
  if (targetIndex < 0 || targetIndex >= displayItems.value.length) return;

  const spacing = currentSpacing.value;
  // 计算目标菜单项应该处于的角度位置
  // 目标角度应该是使该菜单项位于中心位置（角度为0）
  const targetAngle = -targetIndex * spacing;

  // 计算当前角度和目标角度的差值
  const currentAngle = rotation.value;
  let diff = targetAngle - currentAngle;

  // 处理角度循环，选择最短路径
  const total = displayItems.value.length * spacing;
  if (Math.abs(diff) > total / 2) {
    diff = diff > 0 ? diff - total : diff + total;
  }

  // 取消当前动画
  if (requestRef) cancelAnimationFrame(requestRef);
  isSnapAnimating.value = false;
  dragState.value.isDragging = false;

  // 使用 animateToRotation 平滑旋转
  animateToRotation(currentAngle + diff, 350);
};

// 旋转指定数量的菜单项角度
const rotateByItems = (itemCount, direction = 'down') => {
  if (!props.isActive) return;

  const spacing = currentSpacing.value;
  // 计算需要旋转的角度（itemCount 个菜单项的角度）
  const rotateAngle = itemCount * spacing;

  // 根据方向决定旋转方向
  // direction: 'up' 向上（逆时针，减小 rotation），'down' 向下（顺时针，增加 rotation）
  const angleDiff = direction === 'up' ? -rotateAngle : rotateAngle;

  // 取消当前动画
  if (requestRef) cancelAnimationFrame(requestRef);
  isSnapAnimating.value = false;
  dragState.value.isDragging = false;

  // 使用 animateToRotation 平滑旋转
  const currentAngle = rotation.value;
  animateToRotation(currentAngle + angleDiff, 350);
};

// 暴露方法供父组件调用
defineExpose({
  rotateToItem,
  rotateByItems,
});

// 点击菜单项：只负责触发点击音效 + 通知父组件，语音由 active 变化触发
// 点击防抖，避免 500ms 内重复触发
const lastClickTs = ref(0);
const handleItemClick = item => {
  const now = Date.now();
  if (now - lastClickTs.value < 500) return;
  lastClickTs.value = now;

  emit('item-select', item);
};

const handleItemRightClick = useClick({
  onSingleClick: (event, item) => {
    emit('item-right-click', item);
  },
  onDbClick: (event, item) => {
    emit('item-right-db-click', item);
  },
  onTripleClick: (event, item) => {
    emit('item-right-triple-click', item);
  },
});

// 监听 items 的 active 变化：
// 当某个菜单项从 false -> true 且当前环处于激活状态时，播放该项的语音
watch(
    () => props.items || [],
    newItems => {
      if (!props.isActive || !Array.isArray(newItems)) {
        const syncMap = {};
        newItems.forEach(it => {
          syncMap[it.id] = !!it.active;
        });
        prevActiveMap.value = syncMap;
        return;
      }


      const prevMap = prevActiveMap.value || {};


      const nextMap = {};
      newItems.forEach(it => {
        nextMap[it.id] = !!it.active;
      });
      prevActiveMap.value = nextMap;
    },
    {deep: true, immediate: true}
);

watch(
    () => props.resetRotation,
    () => {
      if (!props.isActive) return; // 未激活时不做动画，避免收起卡顿
      isSnapAnimating.value = false;
      dragState.value.isDragging = false;
      animateToRotation(0, 260); // 更快的归零动画
    }
);

onMounted(() => {
  rotation.value = 0;
  requestRef = requestAnimationFrame(animate);
  // 添加键盘事件监听
  window.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
  if (requestRef) cancelAnimationFrame(requestRef);
  // 移除键盘事件监听
  window.removeEventListener('keydown', handleKeyDown);
});
</script>

<style scoped>
.menu-ring-bg2 {
  background-image: url('@/assets/images/menu/menu-ring-bg2.png');
  background-size: 100%;
  background-position: center;
  background-repeat: no-repeat;
  width: 303px;
  height: 840px;
  pointer-events: none;
  position: absolute;
  left: -30px;

}

.menu-ring-bg3 {
  background-image: url('@/assets/images/menu/menu-ring-bg3.png');
  background-size: 100%;
  background-position: center;
  background-repeat: no-repeat;
  width: 303px;
  height: 1080px;
  pointer-events: none;
  position: absolute;
  left: -30px;
}

.levelBg {
  backdrop-filter: blur(5px);
  transition-duration: 700ms;
  transition-property: transform;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 9258px;
  margin-left: -700px;
  height: 995px;
  width: 995px;
  position: absolute;
}

.menu-ring-box {
  transition-duration: 500ms;
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  justify-content: center;
  align-items: center;
  width: 0;
  height: 0;
  display: flex;
  position: absolute;
  left: 0;
  top: 0;
}
</style>
