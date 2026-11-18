import {defineStore} from 'pinia';
import {ref} from 'vue';

export const useMenuStore = defineStore('menu', () => {
  // 状态管理
  const isOpen = ref(false);
  const activeL1 = ref(null);
  const activeL2 = ref(null);
  // 生产监控开关状态
  const productionMonitor = ref(false);

  // 切换菜单打开状态
  const toggleMenu = () => {
    // 生产监控开启时，不允许通过点击菜单按钮去展开/收起
    if (productionMonitor.value) return;
    isOpen.value = !isOpen.value;
    // 关闭时不清空一级菜单选项，保持选中状态以便下次打开时恢复
  };

  // 切换生产监控开关
  const toggleProductionMonitor = val => {
    productionMonitor.value = val;
    if (val) {
      // 开启生产监控：收起转盘菜单
      isOpen.value = false;
    } else {
      // 关闭生产监控：展开转盘菜单
      isOpen.value = true;
    }
  };

  // 选择一级菜单项
  const handleL1Select = item => {
    const wasSelected = activeL1.value === item.id;
    activeL1.value = wasSelected ? null : item.id;
    // 当一级菜单切换时，清空二级菜单选中状态
    if (!wasSelected) {
      activeL2.value = null;
    }
  };

  // 选择二级菜单项
  const handleL2Select = item => {
    activeL2.value = activeL2.value === item.id ? null : item.id;
  };

  // 清空选中状态（可选，用于重置）
  const clearSelection = () => {
    activeL1.value = null;
    activeL2.value = null;
  };

  return {
    isOpen,
    activeL1,
    activeL2,
    productionMonitor,
    toggleMenu,
    toggleProductionMonitor,
    handleL1Select,
    handleL2Select,
    clearSelection
  };
});
