<template>
  <main class="lanhu-page scrollBar">
    <section class="lanhu-shell">
      <section class="lanhu-search">
        <div class="search-box">
          <el-icon class="search-box__icon">
            <Search />
          </el-icon>
          <input
            v-model.trim="keyword"
            class="search-box__input"
            type="text"
            placeholder="查询构件编码、构件型号"
            @keyup.enter="handleSearch"
          />
          <button class="search-box__action" type="button" @click="handleSearch">
            搜索
          </button>
        </div>
      </section>

      <section class="lanhu-section">
        <div class="module-grid">
          <button
            v-for="item in modules"
            :key="item.label"
            type="button"
            class="module-card"
            :class="[
              `module-card--${item.tone}`,
              { active: activeModule === item.label }
            ]"
            @click="activeModule = item.label"
          >
            <span class="module-card__icon">
              <el-icon>
                <component :is="item.icon" />
              </el-icon>
            </span>
            <span class="module-card__label">{{ item.label }}</span>
          </button>
        </div>
        <div class="module-pager" aria-hidden="true">
          <span class="is-active"></span>
          <span></span>
        </div>
      </section>

      <section class="lanhu-section lanhu-data">
        <div class="section-tabs">
          <button
            v-for="item in summaryTabs"
            :key="item.key"
            type="button"
            class="section-tabs__item"
            :class="{ active: activeSummary === item.key }"
            @click="activeSummary = item.key"
          >
            {{ item.label }}
          </button>
        </div>

        <div class="summary-grid">
          <article
            v-for="panel in visiblePanels"
            :key="panel.title"
            class="summary-card"
          >
            <div class="summary-card__tag" :class="`summary-card__tag--${panel.tone}`">
              {{ panel.title }}
            </div>
            <div class="metric-list">
              <div
                v-for="metric in panel.metrics"
                :key="metric.label"
                class="metric-row"
              >
                <div class="metric-row__title">{{ metric.label }}</div>
                <div class="metric-row__value">
                  <span class="metric-row__main">{{ metric.value }}</span>
                  <span class="metric-row__unit">{{ metric.unit }}</span>
                  <span class="metric-row__count">{{ metric.count }}</span>
                </div>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section class="lanhu-footer">
        <nav class="bottom-nav" aria-label="页面导航">
          <button
            type="button"
            class="bottom-nav__item"
            :class="{ active: activeNav === 'home' }"
            @click="activeNav = 'home'"
          >
            <el-icon><House /></el-icon>
            <span>首页</span>
          </button>

          <button type="button" class="bottom-nav__center" @click="activeNav = 'scan'">
            <el-icon>
              <FullScreen />
            </el-icon>
          </button>

          <button
            type="button"
            class="bottom-nav__item"
            :class="{ active: activeNav === 'mine' }"
            @click="activeNav = 'mine'"
          >
            <el-icon><User /></el-icon>
            <span>我的</span>
          </button>
        </nav>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, ref } from 'vue'
import {
  Box,
  DataLine,
  FullScreen,
  House,
  Memo,
  OfficeBuilding,
  RefreshLeft,
  Search,
  Tickets,
  User,
  Van
} from '@element-plus/icons-vue'

const keyword = ref('')
const activeSummary = ref('factory')
const activeNav = ref('home')
const activeModule = ref('项目')

const modules = [
  { label: '项目', icon: OfficeBuilding, tone: 'blue' },
  { label: '订单', icon: Tickets, tone: 'red' },
  { label: '排产', icon: Van, tone: 'cyan' },
  { label: '生产', icon: DataLine, tone: 'blue' },
  { label: '堆存', icon: Box, tone: 'red' },
  { label: '发运', icon: Van, tone: 'cyan' },
  { label: '退货', icon: RefreshLeft, tone: 'violet' },
  { label: '报表', icon: Memo, tone: 'blue' }
]

const summaryTabs = [
  { key: 'factory', label: '工厂数据' },
  { key: 'report', label: '管理日报' }
]

const metrics = {
  factory: [
    { title: '今日', tone: 'pink', metrics: [
      { label: '计划量', value: '1100', unit: 'm³', count: '920块' },
      { label: '浇筑量', value: '400', unit: 'm³', count: '920块' },
      { label: '生产量', value: '390', unit: 'm³', count: '360块' },
      { label: '入库量', value: '500', unit: 'm³', count: '467块' },
      { label: '实时库存', value: '16890', unit: 'm³', count: '5920块' },
      { label: '发运量', value: '578', unit: 'm³', count: '480块' },
      { label: '安装量', value: '430', unit: 'm³', count: '450块' }
    ] },
    { title: '昨日', tone: 'blue', metrics: [
      { label: '计划量', value: '1200', unit: 'm³', count: '980块' },
      { label: '浇筑量', value: '400', unit: 'm³', count: '920块' },
      { label: '生产量', value: '390', unit: 'm³', count: '360块' },
      { label: '入库量', value: '500', unit: 'm³', count: '467块' },
      { label: '实时库存', value: '16890', unit: 'm³', count: '5920块' },
      { label: '发运量', value: '578', unit: 'm³', count: '480块' },
      { label: '安装量', value: '430', unit: 'm³', count: '450块' }
    ] }
  ],
  report: [
    { title: '今日', tone: 'pink', metrics: [
      { label: '计划量', value: '980', unit: 'm³', count: '450块' },
      { label: '浇筑量', value: '430', unit: 'm³', count: '280块' },
      { label: '生产量', value: '520', unit: 'm³', count: '300块' },
      { label: '入库量', value: '600', unit: 'm³', count: '512块' },
      { label: '实时库存', value: '17020', unit: 'm³', count: '6002块' },
      { label: '发运量', value: '640', unit: 'm³', count: '510块' },
      { label: '安装量', value: '490', unit: 'm³', count: '470块' }
    ] },
    { title: '昨日', tone: 'blue', metrics: [
      { label: '计划量', value: '1000', unit: 'm³', count: '460块' },
      { label: '浇筑量', value: '390', unit: 'm³', count: '260块' },
      { label: '生产量', value: '500', unit: 'm³', count: '320块' },
      { label: '入库量', value: '560', unit: 'm³', count: '480块' },
      { label: '实时库存', value: '16890', unit: 'm³', count: '5920块' },
      { label: '发运量', value: '578', unit: 'm³', count: '480块' },
      { label: '安装量', value: '430', unit: 'm³', count: '450块' }
    ] }
  ]
}

const visiblePanels = computed(() => metrics[activeSummary.value])

const handleSearch = () => {
  keyword.value = keyword.value.trim()
}
</script>

<style scoped lang="scss">
.lanhu-page {
  min-height: 100%;
  padding: 24px;
  background: #edf2f8;
  overflow: auto;
}

.lanhu-shell {
  display: grid;
  gap: 20px;
  max-width: 1180px;
  margin: 0 auto;
}

.lanhu-search,
.lanhu-section,
.lanhu-footer {
  width: 100%;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 64px;
  padding: 0 18px;
  background: #fff;
  border: 1px solid #e6edf6;
  border-radius: 18px;
  box-shadow: 0 8px 24px rgb(29 53 87 / 6%);
}

.search-box__icon {
  color: #8f98a8;
  font-size: 20px;
  flex: 0 0 auto;
}

.search-box__input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: none;
  background: transparent;
  font-size: 16px;
  color: #1f2937;
}

.search-box__input::placeholder {
  color: #98a2b3;
}

.search-box__action {
  flex: 0 0 auto;
  border: 0;
  background: transparent;
  color: #1f5fbf;
  font-size: 18px;
  font-weight: 500;
  cursor: pointer;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px 14px;
  padding: 24px 20px 18px;
  background: #fff;
  border: 1px solid #e6edf6;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgb(29 53 87 / 6%);
}

.module-card {
  display: grid;
  justify-items: center;
  gap: 12px;
  padding: 6px 0 8px;
  border: 0;
  border-radius: 16px;
  background: transparent;
  cursor: pointer;
}

.module-card.active {
  background: rgb(31 95 191 / 5%);
}

.module-card__icon {
  display: grid;
  place-items: center;
  width: 72px;
  height: 72px;
  border-radius: 999px;
  color: #fff;
  box-shadow: inset 0 0 0 10px rgb(255 255 255 / 12%), 0 10px 22px rgb(16 51 122 / 18%);
}

.module-card__icon :deep(.el-icon) {
  font-size: 28px;
}

.module-card__label {
  font-size: 20px;
  color: #1f2937;
}

.module-card--blue .module-card__icon {
  background: #2d6fff;
}

.module-card--red .module-card__icon {
  background: #ff3c3c;
}

.module-card--cyan .module-card__icon {
  background: #29c7d2;
}

.module-card--violet .module-card__icon {
  background: #8e58ff;
}

.module-pager {
  display: flex;
  justify-content: center;
  gap: 10px;
  padding-top: 10px;
}

.module-pager span {
  width: 28px;
  height: 4px;
  border-radius: 999px;
  background: #c8d4ea;
}

.module-pager .is-active {
  background: #1f5fbf;
}

.lanhu-data {
  display: grid;
  gap: 18px;
}

.section-tabs {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-left: 4px;
}

.section-tabs::before {
  content: '';
  width: 88px;
  height: 8px;
  margin-right: -70px;
  border-radius: 999px;
  background: #d8e3fb;
}

.section-tabs__item {
  position: relative;
  padding: 0;
  border: 0;
  background: transparent;
  font-size: 28px;
  font-weight: 600;
  color: #8b95a4;
  cursor: pointer;
}

.section-tabs__item.active {
  color: #1f2937;
}

.section-tabs__item.active::after {
  content: '';
  display: block;
  width: 58px;
  height: 8px;
  margin: 4px auto 0;
  border-radius: 999px;
  background: #bad0ff;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.summary-card {
  background: #fff;
  border: 1px solid #e6edf6;
  border-radius: 20px;
  padding: 18px 16px 16px;
  box-shadow: 0 8px 24px rgb(29 53 87 / 6%);
}

.summary-card__tag {
  width: fit-content;
  min-width: 108px;
  margin: -34px auto 14px;
  padding: 10px 18px;
  border-radius: 999px;
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
  text-align: center;
}

.summary-card__tag--pink {
  background: #ffe2e2;
}

.summary-card__tag--blue {
  background: #dbe8ff;
}

.metric-list {
  display: grid;
  gap: 12px;
}

.metric-row {
  display: grid;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #eef4ff 100%);
  border: 1px solid #edf2fa;
}

.metric-row__title {
  font-size: 18px;
  color: #525a69;
  text-align: center;
}

.metric-row__value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.metric-row__main {
  font-size: 30px;
  line-height: 1;
  font-weight: 700;
  color: #1f2937;
}

.metric-row__unit,
.metric-row__count {
  font-size: 30px;
  line-height: 1;
  font-weight: 700;
  color: #1f2937;
}

.lanhu-footer {
  padding-top: 4px;
}

.bottom-nav {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: end;
  gap: 8px;
  padding: 14px 28px 16px;
  background: #fff;
  border: 1px solid #e6edf6;
  border-radius: 24px 24px 0 0;
  box-shadow: 0 -6px 20px rgb(29 53 87 / 4%);
}

.bottom-nav__item {
  display: grid;
  justify-items: center;
  gap: 8px;
  padding: 10px 0 0;
  border: 0;
  background: transparent;
  color: #a3acbb;
  font-size: 22px;
  cursor: pointer;
}

.bottom-nav__item :deep(.el-icon) {
  font-size: 26px;
}

.bottom-nav__item.active {
  color: #1f5fbf;
}

.bottom-nav__center {
  display: grid;
  place-items: center;
  width: 88px;
  height: 88px;
  margin-top: -44px;
  border: 0;
  border-radius: 50%;
  background: linear-gradient(180deg, #4e7ef2 0%, #2056cf 100%);
  color: #fff;
  box-shadow: 0 14px 26px rgb(32 86 207 / 28%);
  cursor: pointer;
}

.bottom-nav__center :deep(.el-icon) {
  font-size: 30px;
}

@media (max-width: 1100px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .module-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .lanhu-page {
    padding: 16px;
  }

  .search-box {
    height: 56px;
    padding: 0 14px;
  }

  .search-box__action,
  .section-tabs__item,
  .module-card__label,
  .metric-row__title,
  .metric-row__main,
  .metric-row__unit,
  .metric-row__count {
    transform: none;
  }

  .module-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px 12px;
    padding: 18px 14px 16px;
  }

  .module-card__icon {
    width: 60px;
    height: 60px;
  }

  .module-card__label {
    font-size: 16px;
  }

  .section-tabs__item {
    font-size: 22px;
  }

  .summary-card {
    padding: 14px 12px 12px;
  }

  .summary-card__tag {
    margin-top: -30px;
    font-size: 18px;
  }

  .metric-row__main,
  .metric-row__unit,
  .metric-row__count {
    font-size: 22px;
  }

  .bottom-nav {
    padding: 12px 18px 14px;
  }

  .bottom-nav__center {
    width: 72px;
    height: 72px;
    margin-top: -36px;
  }
}
</style>
