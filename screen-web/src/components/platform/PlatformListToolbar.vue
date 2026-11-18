<template>
  <section class="toolbar-panel">
    <el-input
        :model-value="keyword"
        class="toolbar-keyword"
        clearable
        :placeholder="keywordPlaceholder"
        @update:model-value="value => emit('update:keyword', value)"
        @keyup.enter="emit('search')"
        @clear="emit('search')"
    />
    <el-select
        v-for="filter in filters"
        :key="filter.name"
        :model-value="filter.value"
        class="toolbar-filter"
        clearable
        :placeholder="filter.placeholder || filter.label"
        @update:model-value="value => emit('update:filter', {name: filter.name, value})"
    >
      <el-option v-for="option in filter.options" :key="String(option)" :label="String(option)" :value="option"/>
    </el-select>
    <div class="toolbar-actions">
      <el-button type="primary" @click="emit('search')">
        <el-icon><Search/></el-icon>
        <span>搜索</span>
      </el-button>
      <el-button @click="emit('reset')">
        <el-icon><RefreshLeft/></el-icon>
        <span>重置</span>
      </el-button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  keyword: {
    type: String,
    default: ''
  },
  keywordPlaceholder: {
    type: String,
    default: '搜索标题或描述'
  },
  filters: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:keyword', 'update:filter', 'search', 'reset'])
</script>

<style scoped lang="scss">
.toolbar-panel {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
  padding: 14px 16px;
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: var(--sx-surface);
}

.toolbar-keyword {
  width: min(320px, 100%);

  :deep(.el-input__wrapper) {
    height: 32px;
  }

  :deep(.el-input__inner) {
    font-size: 14px;
  }
}

.toolbar-filter {
  width: 150px;

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    height: 32px;
  }

  :deep(.el-input__inner),
  :deep(.el-select__placeholder),
  :deep(.el-select__selected-item) {
    font-size: 14px;
  }
}

.toolbar-actions {
  display: flex;
  gap: 10px;

  :deep(.el-button) {
    height: 32px;
  }

  :deep(.el-button + .el-button) {
    margin-left: 0;
  }
}

@media (max-width: 768px) {
  .toolbar-keyword,
  .toolbar-filter {
    width: 100%;
  }

  .toolbar-actions {
    width: 100%;
    margin-left: 0;

    .el-button {
      flex: 1;
    }
  }
}
</style>
