<template>
  <main class="beam-process-page">
    <section class="process-library">
      <header class="panel-header">
        <h1>工艺库</h1>
        <p>您可以拖拽进行排序</p>
      </header>

      <div class="craft-tree scrollBar">
        <article
          v-for="category in craftCategories"
          :key="category.id"
          class="craft-group"
          :class="{ active: category.id === activeCategory }"
        >
          <button class="craft-group__head" type="button" @click="activeCategory = category.id">
            <span class="craft-group__title">
              <img v-if="category.image" :src="category.image" :alt="category.name" />
              <el-icon v-else>
                <component :is="category.icon" />
              </el-icon>
              {{ category.name }}
            </span>
            <span class="craft-group__tools">
              <el-icon><Plus /></el-icon>
              <el-icon>
                <ArrowDown v-if="category.open" />
                <ArrowRight v-else />
              </el-icon>
            </span>
          </button>

          <div v-if="category.open" class="craft-group__children">
            <button
              v-for="craft in category.children"
              :key="craft.id"
              class="craft-card"
              :class="{ active: craft.id === activeCraft }"
              type="button"
              @click="activeCraft = craft.id"
            >
              <span class="craft-card__title">
                {{ craft.name }}
                <span class="craft-card__actions">
                  <el-icon><EditPen /></el-icon>
                  <el-icon><Delete /></el-icon>
                </span>
              </span>
              <span class="craft-card__meta">工艺数量：{{ craft.count }}</span>
            </button>
          </div>
        </article>
      </div>

      <el-button class="cloud-btn" disabled>云端获取</el-button>
    </section>

    <section class="process-content">
      <section class="section-block">
        <div class="section-title-row">
          <h2>工艺列表</h2>
          <el-button class="primary-action" type="primary" @click="handleAdd('工艺')">
            添加工艺
          </el-button>
        </div>

        <div class="craft-list">
          <article
            v-for="item in craftList"
            :key="item.id"
            class="craft-summary"
            :class="{ active: item.id === currentCraft }"
            @click="currentCraft = item.id"
          >
            <div class="summary-main">
              <h3>{{ item.name }}</h3>
              <p>工序数量：{{ item.count }}</p>
              <p>理论时长：{{ item.duration }}</p>
              <p>可用类型：{{ item.types }}</p>
            </div>
            <div class="summary-actions">
              <el-icon><EditPen /></el-icon>
              <el-icon><Delete /></el-icon>
            </div>
          </article>
        </div>
      </section>

      <section class="section-block process-list-block">
        <div class="section-title-row">
          <h2>工序列表</h2>
          <div class="button-group">
            <el-button @click="selectedRows = processRows.map(item => item.id)">
              批量操作
            </el-button>
            <el-button @click="reverseRows">工序顺序定义</el-button>
            <el-button class="primary-action" type="primary" @click="handleAdd('工序')">
              添加工序
            </el-button>
          </div>
        </div>

        <div class="process-list scrollBar">
          <article v-for="row in processRows" :key="row.id" class="process-row">
            <el-checkbox
              class="process-check"
              :model-value="selectedRows.includes(row.id)"
              @change="toggleRow(row.id)"
            />
            <span class="process-index" :class="`process-index--${row.tone}`">
              {{ row.order }}
            </span>
            <div class="process-name">
              <strong>{{ row.name }}</strong>
              <span>工序类型</span>
            </div>
            <dl class="process-meta">
              <div>
                <dt>理论时长</dt>
                <dd>{{ row.duration }}</dd>
              </div>
              <div>
                <dt>进度占比</dt>
                <dd>{{ row.progress }}</dd>
              </div>
              <div>
                <dt>产值占比</dt>
                <dd>{{ row.output }}</dd>
              </div>
              <div>
                <dt>强制完成</dt>
                <dd>{{ row.forceDone }}</dd>
              </div>
              <div>
                <dt>完工工序</dt>
                <dd>{{ row.finishStep }}</dd>
              </div>
              <div>
                <dt>识别颜色</dt>
                <dd class="color-value">
                  <span :style="{ backgroundColor: row.color }"></span>
                </dd>
              </div>
            </dl>
            <button class="icon-btn" type="button" @click="handleEdit(row)">
              <el-icon><EditPen /></el-icon>
            </button>
          </article>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  ArrowRight,
  Box,
  Delete,
  EditPen,
  Finished,
  House,
  OfficeBuilding,
  Plus,
  Van
} from '@element-plus/icons-vue'
import iconPrefabricate from '@/assets/lanhu/beam-process/icon_prefabricate@2x.png'
import iconStorehouse from '@/assets/lanhu/beam-process/icon_storehouse@2x.png'

const activeCategory = ref('prefabricate')
const activeCraft = ref('prefabricate-2')
const currentCraft = ref('craft-2')
const selectedRows = ref([])

const craftCategories = [
  {
    id: 'prefabricate',
    name: '预制工艺',
    icon: OfficeBuilding,
    image: iconPrefabricate,
    open: true,
    children: [
      { id: 'prefabricate-1', name: '{工艺小分类}', count: 4 },
      { id: 'prefabricate-2', name: '{工艺小分类}', count: 4 }
    ]
  },
  { id: 'store', name: '存储工艺', icon: House, image: iconStorehouse, open: false, children: [] },
  { id: 'transport', name: '运输工艺', icon: Van, open: false, children: [] },
  { id: 'erect', name: '架设工艺', icon: Finished, open: false, children: [] },
  { id: 'site', name: '现场工艺', icon: Box, open: false, children: [] }
]

const craftList = [
  {
    id: 'craft-1',
    name: '工艺名称',
    count: 1,
    duration: '240小时',
    types: '{梁类型}、{梁类型}、{梁类型}、{梁类型}、{梁类型}、...'
  },
  {
    id: 'craft-2',
    name: '工艺名称',
    count: 7,
    duration: '240小时',
    types: '{梁类型}、{梁类型}、{梁类型}、{梁类型}、{梁类型}、...'
  },
  {
    id: 'craft-3',
    name: '工艺名称',
    count: 1,
    duration: '240小时',
    types: '{梁类型}、{梁类型}、{梁类型}、{梁类型}、{梁类型}、...'
  },
  {
    id: 'craft-4',
    name: '工艺名称',
    count: 1,
    duration: '240小时',
    types: '{梁类型}、{梁类型}、{梁类型}、{梁类型}、{梁类型}、...'
  }
]

const processRows = ref([
  { id: 'a', order: 1, name: '工序名称A', duration: '5小时', progress: '50%', output: '50%', forceDone: '是', finishStep: '是', color: 'rgba(91,66,255,1)', tone: 'purple' },
  { id: 'b', order: 2, name: '工序名称B', duration: '5小时', progress: '50%', output: '50%', forceDone: '是', finishStep: '是', color: 'rgba(91,66,255,1)', tone: 'green' },
  { id: 'c', order: 3, name: '工序名称C', duration: '5小时', progress: '50%', output: '50%', forceDone: '是', finishStep: '是', color: 'rgba(91,66,255,1)', tone: 'green' },
  { id: 'd', order: 4, name: '工序名称D', duration: '5小时', progress: '50%', output: '50%', forceDone: '是', finishStep: '是', color: 'rgba(91,66,255,1)', tone: 'purple' },
  { id: 'e', order: 5, name: '工序名称E', duration: '5小时', progress: '50%', output: '50%', forceDone: '是', finishStep: '是', color: 'rgba(91,66,255,1)', tone: 'purple' },
  { id: 'f', order: 6, name: '工序名称F', duration: '5小时', progress: '50%', output: '50%', forceDone: '是', finishStep: '是', color: 'rgba(91,66,255,1)', tone: 'purple' }
])

const toggleRow = id => {
  selectedRows.value = selectedRows.value.includes(id)
    ? selectedRows.value.filter(item => item !== id)
    : [...selectedRows.value, id]
}

const reverseRows = () => {
  processRows.value = [...processRows.value].reverse().map((item, index) => ({
    ...item,
    order: index + 1
  }))
}

const handleAdd = type => {
  ElMessage.success(`已触发添加${type}`)
}

const handleEdit = row => {
  ElMessage.info(`编辑${row.name}`)
}
</script>

<style scoped lang="scss">
.beam-process-page {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 0;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  background: rgba(255, 255, 255, 1);
  border-radius: 8px;
  color: rgba(0, 0, 0, 0.9);
  font-family: "PingFang SC", "Microsoft YaHei", "Hiragino Sans GB", sans-serif;
}

.process-library,
.process-content {
  min-height: 0;
  background: transparent;
}

.process-library {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 26px 20px 20px;
  border-right: 1px solid rgba(210, 208, 219, 1);
}

.panel-header h1,
.section-title-row h2 {
  margin: 0;
  color: rgba(0, 0, 0, 0.9);
  font-size: 20px;
  font-weight: 600;
  line-height: 28px;
}

.panel-header p {
  margin: 8px 0 20px;
  color: rgba(0, 0, 0, 0.5);
  font-size: 14px;
  line-height: 18px;
}

.craft-tree {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.craft-group {
  overflow: hidden;
  border: 1px dashed rgba(210, 208, 219, 1);
  border-radius: 6px;
  background: rgba(255, 255, 255, 1);
}

.craft-group.active {
  border-color: rgba(210, 208, 219, 1);
}

.craft-group__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 48px;
  padding: 0 18px;
  border: 0;
  background: transparent;
  color: rgba(0, 0, 0, 0.9);
  cursor: pointer;
}

.craft-group__title,
.craft-group__tools,
.craft-card__actions,
.summary-actions,
.button-group {
  display: inline-flex;
  align-items: center;
}

.craft-group__title {
  gap: 8px;
  font-size: 14px;
  line-height: 22px;
}

.craft-group__title img {
  width: 16px;
  height: 16px;
  object-fit: contain;
}

.craft-group__tools {
  gap: 18px;
  color: rgba(0, 0, 0, 0.5);
  font-size: 14px;
}

.craft-group__children {
  display: grid;
  gap: 20px;
  padding: 12px 20px 20px;
}

.craft-card {
  height: 96px;
  padding: 20px;
  border: 1px solid rgba(210, 208, 219, 1);
  border-radius: 6px;
  background: rgba(255, 255, 255, 1);
  text-align: left;
  cursor: pointer;
}

.craft-card.active {
  border-color: transparent;
  background: linear-gradient(135deg, rgba(91, 66, 255, 1) 0%, rgba(139, 55, 255, 1) 100%);
  color: rgba(255, 255, 255, 1);
}

.craft-card__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

.craft-card__actions {
  gap: 18px;
  color: rgba(0, 0, 0, 0.5);
  font-size: 14px;
}

.craft-card.active .craft-card__actions {
  color: rgba(255, 255, 255, 0.8);
}

.craft-card__meta {
  display: block;
  margin-top: 12px;
  color: rgba(0, 0, 0, 0.5);
  font-size: 14px;
  line-height: 22px;
}

.craft-card.active .craft-card__meta {
  color: rgba(255, 255, 255, 0.8);
}

.cloud-btn {
  align-self: center;
  width: 88px;
  margin-top: 22px;
}

.process-content {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 34px;
  padding: 26px 20px 20px;
}

.section-block {
  min-width: 0;
}

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.button-group {
  gap: 16px;
}

.primary-action {
  --el-button-bg-color: rgba(91, 66, 255, 1);
  --el-button-border-color: rgba(91, 66, 255, 1);
  --el-button-hover-bg-color: rgba(105, 78, 255, 1);
  --el-button-hover-border-color: rgba(105, 78, 255, 1);
  --el-button-active-bg-color: rgba(78, 54, 232, 1);
  --el-button-active-border-color: rgba(78, 54, 232, 1);
}

.craft-list {
  display: flex;
  flex-wrap: nowrap;
  gap: 20px;
  overflow: hidden;
}

.craft-summary {
  position: relative;
  flex: 0 0 440px;
  min-height: 154px;
  padding: 20px;
  border: 1px solid rgba(210, 208, 219, 1);
  border-radius: 6px;
  background: rgba(255, 255, 255, 1);
  cursor: pointer;
}

.craft-summary.active {
  border-color: transparent;
  background: linear-gradient(135deg, rgba(91, 66, 255, 1) 0%, rgba(139, 55, 255, 1) 100%);
}

.summary-main h3 {
  max-width: calc(100% - 56px);
  margin: 0 0 10px;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.9);
  font-size: 20px;
  font-weight: 600;
  line-height: 28px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-main p {
  margin: 0 0 8px;
  overflow: hidden;
  color: rgba(0, 0, 0, 0.5);
  font-size: 14px;
  line-height: 22px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.craft-summary.active h3,
.craft-summary.active p {
  color: rgba(255, 255, 255, 1);
}

.craft-summary.active p {
  opacity: 0.86;
}

.summary-actions {
  position: absolute;
  top: 22px;
  right: 18px;
  gap: 18px;
  color: rgba(0, 0, 0, 0.5);
  font-size: 16px;
}

.craft-summary.active .summary-actions {
  color: rgba(255, 255, 255, 0.8);
}

.process-list-block {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
}

.process-list {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
  padding-right: 2px;
}

.process-row {
  display: grid;
  grid-template-columns: 24px 40px minmax(170px, 220px) minmax(520px, 1fr) 28px;
  align-items: center;
  gap: 20px;
  min-height: 80px;
  padding: 16px 16px 16px 0;
  border: 1px solid rgba(210, 208, 219, 1);
  border-radius: 6px;
  background: rgba(255, 255, 255, 1);
}

.process-check {
  justify-self: center;
}

.process-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 4px;
  color: rgba(255, 255, 255, 1);
  font-size: 24px;
  font-weight: 600;
  line-height: 32px;
}

.process-index--purple {
  background: rgba(91, 66, 255, 1);
}

.process-index--green {
  background: rgba(31, 132, 10, 1);
}

.process-name {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.process-name strong {
  overflow: hidden;
  color: rgba(0, 0, 0, 0.9);
  font-size: 18px;
  font-weight: 600;
  line-height: 26px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.process-name span {
  flex: 0 0 auto;
  padding: 1px 8px;
  border: 1px solid rgba(53, 105, 238, 0.3);
  border-radius: 3px;
  background: rgba(53, 105, 238, 0.08);
  color: rgba(53, 105, 238, 1);
  font-size: 14px;
  line-height: 18px;
}

.process-meta {
  display: grid;
  grid-template-columns: repeat(6, minmax(78px, 1fr));
  gap: 14px;
  margin: 0;
}

.process-meta div {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 6px;
}

.process-meta dt,
.process-meta dd {
  margin: 0;
  font-size: 14px;
  line-height: 22px;
  white-space: nowrap;
}

.process-meta dt {
  color: rgba(0, 0, 0, 0.5);
}

.process-meta dd {
  color: rgba(0, 0, 0, 0.9);
}

.process-meta dt::after {
  content: ":";
  margin-left: 2px;
}

.color-value span {
  display: block;
  width: 20px;
  height: 20px;
  border-radius: 2px;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 0;
  background: transparent;
  color: rgba(0, 0, 0, 0.5);
  cursor: pointer;
}

@media (max-width: 1280px) {
  .beam-process-page {
    grid-template-columns: 260px minmax(0, 1fr);
  }

  .process-row {
    grid-template-columns: 24px 40px minmax(150px, 200px) minmax(480px, 1fr) 28px;
  }
}
</style>
