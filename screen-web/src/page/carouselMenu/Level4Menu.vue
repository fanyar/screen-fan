<template>
  <div
      ref="ringRef"
      class="level-box"
      :style="{
      zIndex: 10,
      transform: isActive ? 'scale(1)' : 'scale(0.5)',
      opacity: isActive ? 1 : 0,
      pointerEvents: isActive ? 'auto' : 'none',
    }"
  >
    <div
        class="levelBg"
        :style="{
        transform: `rotate(${rotation * -0.5 + rotationOffset}deg)`,
        zIndex: 0,
      }"
    />

    <!-- 菜单项-->
    <div
        v-for="(item, index) in menuItems"
        :key="item.id"
        style="position: absolute"
        :class="{
        'cursor-pointer': !item.isSpecial || item.canPaginate,
        'cursor-not-allowed opacity-50': item.isSpecial && !item.canPaginate,
      }"
        :style="{
        transform: `translate3d(${getPosRem(getItemAngle(item.angleSlotIndex), radius, item.offsetX || 0).x}, ${getPosRem(getItemAngle(item.angleSlotIndex), radius, item.offsetX || 0).y}, 0)`,
        zIndex: 1000,
      }"
        @click="handleItemClick(item)"
    >
      <div
          style="position: relative; display: flex; align-items: center; color: #fff;"
          :style="{
          width: (item.isSpecial ? 621 : 650) + 'px',
          height: (item.isSpecial ? 68 : 86) + 'px',
          justifyContent: 'flex-start',
          paddingLeft: (item.textOffset ?? 80) + 'px',
        }"
      >
        <!-- 原背景图片（level 4 始终使用基础版本，不受 active 影响） -->
        <div
            style="pointer-events: none; position: absolute; inset: 0"
            :class="{
            '-ml-[36%]': !item.isSpecial,
          }"
            :style="{
            backgroundImage: `url(${getImageUrl(`menu/${item.icon}.png`)})`,
            backgroundSize: '100% 100%',
            backgroundRepeat: 'no-repeat',
            backgroundPosition: 'center',
            zIndex: 1,
            marginLeft: !item.isSpecial ? '-36%' : 'inherit'
          }"
        />
        <!-- 边框背景图片（当active时显示，但特殊项不显示） -->
        <div
            v-if="!item.isSpecial"
            style="pointer-events: none; position: absolute; inset: 0"
            :style="{
            backgroundImage: `url(${getImageUrl(`menu/level4-active-${item.activeImageIndex}.png`)})`,
            backgroundSize: '100% 100%',
            backgroundRepeat: 'no-repeat',
            backgroundPosition: 'center',
            zIndex: 2,
          }"
        />
        <!-- 特殊项：显示翻页文本和三角形图标 -->
        <template v-if="item.isSpecial && item.paginationText">
          <div
              style="position: relative; z-index: 10; display: flex; align-items: center; gap: 8px"
              :class="{
              'page-arrow-up': item.dataIndex === 0,
              'page-arrow-down': item.dataIndex === 7,
            }"
          >
            <span
                style="overflow: hidden;text-overflow: ellipsis;white-space: nowrap;"
                :style="{
                fontSize: '16px',
                textShadow: '0 0 8px #8542FF, 0 0 12px #8542FF',
              }"
            >
              {{ item.paginationText }}
            </span>
          </div>
        </template>
        <!-- 普通项：显示原有文本 -->
        <span
            v-else
            style="position: relative; z-index: 10; padding-left: 16px; padding-right: 16px; overflow: hidden;text-overflow: ellipsis;white-space: nowrap;"
            :style="{
            fontSize: (item.active ? 18 : 16) + 'px',
            textShadow: item.active
              ? '0 0 8px #8542FF, 0 0 12px #8542FF, 0 0 16px #8542FF, 0 0 20px #8542FF, 0 0 24px #8542FF, 0 0 32px #8542FF'
              : 'none',
            fontWeight: item.active ? 'bold' : 'normal',
          }"
        >
          {{ item.label }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, ref, watch, nextTick, onMounted, onUnmounted} from 'vue';
import {pxToRem, getImageUrl} from '@/util';
import {ElMessage} from 'element-plus';
import {useBasicStore} from "@/store"

const props = defineProps({
  isActive: {
    type: Boolean,
    default: false,
  },
  resetRotation: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['item-select']);
const useBasic = useBasicStore();

// level 4 菜单项选中状态
const activeL3ItemId = ref(null);
// level 4 菜单当前显示模式：'pagination'（翻页模式，显示1和8）或 'normal'（正常模式，显示2-7）
const l3DisplayMode = ref('normal');
// 翻页模式下的当前页起始索引（中间6个位置显示的数据起始索引）
const currentPageStartIndex = ref(0);

const radius = 920;
const centerOffsetX = 400;
const verticalSpacing = 127;
const rotationOffset = 6;
const rotation = ref(0);

// 键盘节流（W/S/↑/↓）
let lastKeyPressTime = 0;
const KEY_THROTTLE_MS = 200;

// 计算间距角度
const currentSpacing = computed(() => (verticalSpacing / radius) * (180 / Math.PI));

// 居中偏移（level 4 始终按照7个位置来计算中心点）
const listCenter = computed(() => {
  const fixedCount = 7;
  return ((fixedCount - 1) * currentSpacing.value) / 2;
});

const lineDataSource = computed(() => {
  const storeData = useBasic.MENU_LEVEL_FOUR || [];
  return storeData;
});

// 三级菜单：使用 lineByBeamFamily 数据
// 数据 > 6：翻页模式，使用 8 个槽位（0 和 7 为翻页按钮，1-6 为数据）
// 数据 <= 6：普通模式，只使用槽位 1-6 显示数据
const menuItems = computed(() => {
  const lineData = lineDataSource.value;
  const totalCount = lineData.length;

  // 预设的 offsetX、textOffset 和 angleOffset 配置（对应 0-7 共 8 个槽位）
  const positionConfigs = [
    {offsetX: 78, textOffset: 90, angleOffset: 0, isSpecial: true}, // 槽位0：翻页特殊样式
    {offsetX: 7, textOffset: 180, angleOffset: 0, isSpecial: false}, // 槽位1
    {offsetX: -30, textOffset: 260, angleOffset: 0, isSpecial: false}, // 槽位2
    {offsetX: -50, textOffset: 310, angleOffset: 0, isSpecial: false}, // 槽位3
    {offsetX: -50, textOffset: 310, angleOffset: 0, isSpecial: false}, // 槽位4
    {offsetX: -30, textOffset: 270, angleOffset: 0, isSpecial: false}, // 槽位5
    {offsetX: 0, textOffset: 180, angleOffset: 0, isSpecial: false}, // 槽位6
    {offsetX: 68, textOffset: 90, angleOffset: 0, isSpecial: true}, // 槽位7：翻页特殊样式
  ];

  console.log('[Level4Menu] lineData totalCount=', totalCount);

  // 普通模式：数据 <= 6，只占用槽位 1-6，不出现翻页按钮
  if (totalCount <= 6) {
    l3DisplayMode.value = 'normal';

    return lineData.map((item, dataIndex) => {
      const slotIndex = dataIndex + 1; // 槽位 1-6
      const config = positionConfigs[slotIndex] || {offsetX: 0, textOffset: 80, angleOffset: 0, isSpecial: false};
      const itemId = item.id;

      return {
        id: itemId,
        label: item.lineName,
        icon: `level4-${slotIndex + 1}`,
        offsetX: config.offsetX,
        textOffset: config.textOffset,
        angleOffset: config.angleOffset,
        active: activeL3ItemId.value === itemId,
        isSpecial: false,
        angleSlotIndex: slotIndex,
        dataIndex,
        isPaginationMode: false,
        paginationText: null,
        activeImageIndex: slotIndex,
      };
    });
  }

  // 翻页模式：数据 > 6，使用槽位 0-7，其中 0 和 7 为翻页按钮
  l3DisplayMode.value = 'pagination';

  const items = [];
  const pageSize = 6;
  const maxStartIndex = Math.max(0, totalCount - 1);
  const pageStartIndex = Math.min(currentPageStartIndex.value, maxStartIndex);

  console.log('[Level4Menu] mode= pagination(>6)');
  console.log('[Level4Menu] currentPageStartIndex=', currentPageStartIndex.value);

  // 槽位0：向上翻页按钮
  {
    const slotIndex = 0;
    const config = positionConfigs[slotIndex] || {offsetX: 0, textOffset: 80, angleOffset: 0, isSpecial: true};

    items.push({
      id: 'pagination-up',
      label: '',
      icon: `level4-1`,
      offsetX: config.offsetX,
      textOffset: config.textOffset,
      angleOffset: config.angleOffset,
      active: false,
      isSpecial: true,
      angleSlotIndex: slotIndex,
      dataIndex: 0,
      isPaginationMode: true,
      paginationText: '向上翻页',
      canPaginate: currentPageStartIndex.value > 0,
      activeImageIndex: 1,
    });
  }

  // 槽位1-6：当前页的 6 条数据
  for (let i = 0; i < pageSize; i++) {
    const dataIndex = pageStartIndex + i;
    if (dataIndex >= totalCount) break;

    const slotIndex = i + 1;
    const config = positionConfigs[slotIndex] || {offsetX: 0, textOffset: 80, angleOffset: 0, isSpecial: false};
    const item = lineData[dataIndex];
    const itemId = item.id || `l3-${dataIndex + 1}`;

    items.push({
      id: itemId,
      label: item.lineName,
      icon: `level4-${slotIndex + 1}`,
      offsetX: config.offsetX,
      textOffset: config.textOffset,
      angleOffset: config.angleOffset,
      active: activeL3ItemId.value === itemId,
      isSpecial: false,
      angleSlotIndex: slotIndex,
      dataIndex,
      isPaginationMode: true,
      paginationText: null,
      activeImageIndex: slotIndex,
    });
  }

  // 槽位7：向下翻页按钮
  {
    const slotIndex = 7;
    const config = positionConfigs[slotIndex] || {offsetX: 0, textOffset: 80, angleOffset: 0, isSpecial: true};
    const canPaginateDown = currentPageStartIndex.value < maxStartIndex;

    items.push({
      id: 'pagination-down',
      label: '',
      icon: `level4-8`,
      offsetX: config.offsetX,
      textOffset: config.textOffset,
      angleOffset: config.angleOffset,
      active: false,
      isSpecial: true,
      angleSlotIndex: slotIndex,
      dataIndex: 7,
      isPaginationMode: true,
      paginationText: '向下翻页',
      canPaginate: canPaginateDown,
      activeImageIndex: 6,
    });
  }

  console.log(
      '[Level4Menu] pagination items dataIndex=',
      items.map(i => i.dataIndex)
  );

  return items;
});

// 角度计算
const getItemAngle = index => {
  const spacing = currentSpacing.value;
  // 关键：角度槽位是固定的（0-7），不能用当前渲染数量做取模基数。
  // 否则当数据很少（例如 1 条）时，(index * spacing) % (count * spacing) 会直接归零，导致永远落在“槽位0”。
  const slotCount = 8;

  // 基础槽位角度
  let angle = index * spacing;
  // level 4 添加初始角度偏移
  angle += rotationOffset || 0;

  const total = slotCount * spacing;
  angle = ((angle % total) + total) % total;

  let finalAngle = angle - listCenter.value;

  // level 4 菜单向上逆时针偏移（减小角度值）
  finalAngle -= 9;

  return finalAngle;
};

// 位置计算
const getPosRem = (angleInDegrees, radius, offsetX = 0) => {
  const rad = (angleInDegrees * Math.PI) / 180;
  const x = Math.cos(rad) * radius - centerOffsetX + offsetX;
  const y = Math.sin(rad) * radius;
  return {
    x: x + 'px',//pxToRem(x),
    y: y + 'px'//pxToRem(y),
  };
};

const showNoMorePageMessage = direction => {
  ElMessage({
    type: 'warning',
    message: direction === 'up' ? '已到第一页' : '已到最后一页',
  });
};

const tryPaginate = direction => {
  const pageSize = 6;
  const totalCount = lineDataSource.value.length;
  // 允许最后一页不足 pageSize（例如 7 条数据时，最后一页只剩第 7 条）
  const maxStartIndex = Math.max(0, totalCount - 1);

  if (direction === 'up') {
    if (currentPageStartIndex.value <= 0) {
      showNoMorePageMessage('up');
      return false;
    }
    currentPageStartIndex.value = Math.max(0, currentPageStartIndex.value - pageSize);
    return true;
  }

  // down
  if (currentPageStartIndex.value >= maxStartIndex) {
    showNoMorePageMessage('down');
    return false;
  }
  currentPageStartIndex.value = Math.min(maxStartIndex, currentPageStartIndex.value + pageSize);
  return true;
};

// 键盘控制：W/S/↑/↓ 按“项”选择，并在页首/尾自动翻页
const handleKeyDown = e => {
  // 只在三级菜单激活时响应
  if (!props.isActive) return;

  // 只处理 W/S/↑/↓ 键
  const isUpKey = e.key === 'w' || e.key === 'W' || e.key === 'ArrowUp';
  const isDownKey = e.key === 's' || e.key === 'S' || e.key === 'ArrowDown';

  if (!isUpKey && !isDownKey) return;

  // 节流控制
  const now = Date.now();
  if (now - lastKeyPressTime < KEY_THROTTLE_MS) return;
  lastKeyPressTime = now;

  // 避免与外层冲突
  e.preventDefault();
  e.stopPropagation();

  const totalCount = lineDataSource.value.length;
  if (!totalCount) return;

  const pageSize = 6;
  const maxStartIndex = Math.max(0, totalCount - 1);

  // 计算当前页起止下标
  let pageStartIndex = 0;
  if (totalCount > pageSize) {
    pageStartIndex = Math.min(currentPageStartIndex.value, maxStartIndex);
  }
  let pageEndIndex = Math.min(pageStartIndex + pageSize - 1, totalCount - 1);

  // 当前选中项在整体数据中的下标
  let currentIndex = lineDataSource.value.findIndex(item => item.id === activeL3ItemId.value);
  if (currentIndex === -1) {
    // 尚未有选中项时，默认从当前页第一条开始
    currentIndex = pageStartIndex;
  }

  if (isUpKey) {
    // 页内向上移动
    if (currentIndex > pageStartIndex) {
      currentIndex -= 1;
    } else {
      // 已经在当前页第一条，尝试向上翻页
      if (pageStartIndex === 0) {
        showNoMorePageMessage('up');
        return;
      }
      const ok = tryPaginate('up');
      if (!ok) return;

      // 翻到上一页后，选中新页最后一条
      const newTotal = lineDataSource.value.length;
      const newMaxStart = Math.max(0, newTotal - 1);
      const newPageStart = Math.min(currentPageStartIndex.value, newMaxStart);
      const newPageEnd = Math.min(newPageStart + pageSize - 1, newTotal - 1);
      currentIndex = newPageEnd;
    }
  } else if (isDownKey) {
    // 页内向下移动
    if (currentIndex < pageEndIndex) {
      currentIndex += 1;
    } else {
      // 已经在当前页最后一条，尝试向下翻页
      if (pageStartIndex === maxStartIndex) {
        showNoMorePageMessage('down');
        return;
      }
      const ok = tryPaginate('down');
      if (!ok) return;

      // 翻到下一页后，选中新页第一条
      const newTotal = lineDataSource.value.length;
      const newMaxStart = Math.max(0, newTotal - 1);
      const newPageStart = Math.min(currentPageStartIndex.value, newMaxStart);
      currentIndex = newPageStart;
    }
  }

  const target = lineDataSource.value[currentIndex];
  if (!target) return;

  // 同步内部选中状态与 store
  activeL3ItemId.value = target.id;

  // 与点击逻辑保持一致：向父组件派发选中事件
  const menuItem = menuItems.value.find(m => m.id === target.id);
  if (menuItem) {
    emit('item-select', menuItem);
  }
};

// 点击处理
const handleItemClick = item => {
  // 如果是翻页按钮（特殊项），进行翻页
  if (item.isSpecial && item.isPaginationMode) {
    if (item.dataIndex === 0) {
      // 点击第1个：向上翻页（上一页）
      tryPaginate('up');
    } else if (item.dataIndex === 7) {
      // 点击第8个：向下翻页（下一页）
      tryPaginate('down');
    }
    // 特殊项不切换active状态，只进行翻页
    emit('item-select', item);
    return;
  }

  // 普通项：切换选中状态
  if (activeL3ItemId.value === item.id) {
    activeL3ItemId.value = null;
  } else {
    activeL3ItemId.value = item.id;
  }
  emit('item-select', item);
};

// 监听 resetRotation
watch(
    () => props.resetRotation,
    () => {
      if (props.resetRotation) {
        rotation.value = 0;
      }
    }
);

// 监听数据变化，重置翻页索引（当数据变化时，重置到第一页）
watch(
    () => lineDataSource.value.length,
    () => {
      currentPageStartIndex.value = 0;
    }
);

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown);
});
</script>

<style scoped>
.level-box {
  transition-duration: 500ms;
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  justify-content: center;
  align-items: center;
  width: 0px;
  height: 0;
  display: flex;
  position: absolute;
  left: 0;
  top: 0;

}


.levelBg {
  backdrop-filter: blur(5px);
  transition-duration: 700ms;
  transition-property: transform;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 9258px;
  width: 1500px;
  height: 1500px;
  margin-left: -810px;
  position: absolute;
}

.cursor-pointer {
  cursor: pointer;
}

.cursor-not-allowed {
  cursor: not-allowed;
}

.opacity-50 {
  opacity: 0.5;
}

/* 翻页文字两侧三角形：使用伪元素实现，便于统一控制样式 */
.page-arrow-up::before,
.page-arrow-up::after,
.page-arrow-down::before,
.page-arrow-down::after {
  content: '';
  display: inline-block;
  width: 10px;
  height: 10px;
  background-color: rgba(255, 255, 255, 0.4);
}

.page-arrow-up::before,
.page-arrow-down::before {
  margin-right: 4px;
}

.page-arrow-up::after,
.page-arrow-down::after {
  margin-left: 4px;
}

/* 向上三角形 */
.page-arrow-up::before,
.page-arrow-up::after {
  clip-path: polygon(50% 0%, 0% 100%, 100% 100%);
}

/* 向下三角形 */
.page-arrow-down::before,
.page-arrow-down::after {
  clip-path: polygon(0% 0%, 100% 0%, 50% 100%);
}
</style>
