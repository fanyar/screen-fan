<template>
  <div class="h-screen w-full overflow-y-auto bg-white text-[14px] text-[#1f2937] scrollBar">
    <header class="sticky top-0 z-20 border-b border-[#e8edf5] bg-white/90 backdrop-blur">
      <div class="home-header-inner mx-auto flex h-[64px] w-full max-w-[1200px] items-center justify-between px-[24px]">
        <button class="flex items-center gap-[10px]" @click="$router.push('/home')">
          <img class="h-[28px]" :src="logoFan" :alt="platformName"/>
          <span v-if="hasText(platformName)" class="home-brand-name text-[18px] font-semibold text-[#1f2937]">{{ platformName }}</span>
        </button>
        <nav class="hidden items-center gap-[32px] md:flex">
          <button v-if="introVisible" class="text-[14px] text-[#495770] transition-colors hover:text-sx-primary"
                  @click="scrollToSection('#platform-intro')">平台介绍
          </button>
          <button v-if="featuresVisible" class="text-[14px] text-[#495770] transition-colors hover:text-sx-primary"
                  @click="scrollToSection('#product-features')">产品特性
          </button>
          <button v-if="contactVisible" class="text-[14px] text-[#495770] transition-colors hover:text-sx-primary"
                  @click="scrollToSection('#contact-us')">联系我们
          </button>
        </nav>
        <el-button type="primary" class="!h-[38px] !px-[22px] !text-[14px]"
                   @click="$router.push('/platform')">进入平台
        </el-button>
      </div>
    </header>

    <section class="home-hero relative flex h-[72vh] max-h-[820px] min-h-[560px] w-full items-center overflow-hidden bg-sx-navy">
      <video class="absolute inset-0 h-full w-full object-cover" loop="loop" muted="muted" preload="auto"
             autoplay="autoplay" playsinline>
        <source v-if="home.heroVideoUrl" :src="home.heroVideoUrl">
        <source v-else src="@/assets/screen/visible.mp4">
      </video>
      <div
          class="absolute inset-0 bg-gradient-to-r from-[rgba(4,12,24,0.88)] via-[rgba(4,12,24,0.55)] to-[rgba(4,12,24,0.15)]"></div>
      <div class="home-hero-inner relative mx-auto w-full max-w-[1200px] px-[24px]">
        <div class="max-w-[640px] pb-[24px]">
          <span v-if="hasText(home.heroBadge)"
              class="mb-[18px] inline-flex items-center rounded-[4px] border border-[rgba(34,211,238,0.45)] bg-[rgba(34,211,238,0.12)] px-[12px] py-[6px] text-[14px] text-sx-accent">{{ home.heroBadge }}</span>
          <h1 v-if="hasText(home.heroTitle)" class="home-hero-title text-[44px] font-semibold leading-[58px] text-white">{{ home.heroTitle }}</h1>
          <p v-if="hasText(home.heroDescription)" class="home-hero-desc mt-[16px] max-w-[560px] text-[16px] leading-[28px] text-[#d8e9ff]">
            {{ home.heroDescription }}
          </p>
          <div v-if="heroActionsVisible" class="home-hero-actions mt-[36px] flex flex-wrap items-center gap-[16px]">
            <el-button v-if="hasText(home.heroPrimaryButtonText)" type="primary" class="!h-[48px] !px-[28px] !text-[16px]"
                       @click="$router.push('/platform')">{{ home.heroPrimaryButtonText }}
            </el-button>
            <button
                v-if="hasText(home.heroSecondaryButtonText)"
                class="flex h-[48px] items-center rounded-[4px] border border-[rgba(255,255,255,0.55)] px-[26px] text-[16px] text-white transition-colors hover:border-white hover:bg-white/10"
                @click="scrollToSection('#product-features')">{{ home.heroSecondaryButtonText }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <section v-if="introVisible" id="platform-intro"
             class="scroll-mt-[88px] w-full bg-cover"
             :class="{'home-introduce-bg': hasText(home.introBackgroundUrl)}"
             :style="introStyle">
      <div class="home-intro-inner mx-auto w-full max-w-[1200px] px-[24px] py-[110px]">
        <div class="max-w-[560px]">
          <div v-if="hasText(home.introTitle)" class="home-intro-title pb-[12px] text-[40px] font-medium leading-[52px] text-[#111827]">{{ home.introTitle }}</div>
          <div v-if="hasText(home.introDescription)" class="home-intro-desc text-[16px] leading-[28px] text-[#495770]">
            {{ home.introDescription }}
          </div>
        </div>
      </div>
    </section>

    <section v-if="featuresVisible" id="product-features" class="home-features scroll-mt-[88px] py-[90px]">
      <div class="mx-auto w-full max-w-[1200px] px-[24px]">
        <div v-if="hasText(home.featureTitle)" class="home-section-title mb-[50px] text-center text-[28px] font-medium leading-[40px] text-[#111827]">{{ home.featureTitle }}</div>
        <el-row :gutter="24">
          <el-col v-for="item in visibleFeatures" :key="item.title || item.image || item.desc" :xs="24" :sm="12" :md="8">
            <div
                class="mb-[24px] flex h-full rounded-[6px] border border-[#e8edf5] bg-white p-[24px] transition-colors hover:border-[#92b1f5]">
              <img v-if="item.image" class="mr-[20px] h-[48px] w-[48px] shrink-0" :src="item.image" :alt="item.title"/>
              <div class="min-w-0 flex-1">
                <div class="mb-[8px] truncate text-[18px] font-normal leading-[28px] text-[#111827]">
                  {{ item.title }}
                </div>
                <div class="text-[14px] leading-[22px] text-[#495770]">{{ item.desc }}</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </section>

    <section v-if="contactVisible" id="contact-us" class="home-contact scroll-mt-[88px] bg-[#f5f7fa] py-[90px]">
      <div class="mx-auto w-full max-w-[1200px] px-[24px] text-center">
        <div v-if="hasText(home.contactTitle)" class="home-section-title text-[28px] font-medium leading-[40px] text-[#111827]">{{ home.contactTitle }}</div>
        <div v-if="hasText(home.contactDescription)" class="home-contact-desc mt-[14px] text-[16px] leading-[26px] text-[#495770]">{{ home.contactDescription }}</div>
        <div v-if="hasText(home.contactPhone)" class="home-contact-phone mt-[26px] text-[30px] font-semibold leading-[42px] text-sx-primary">{{ home.contactPhone }}</div>
      </div>
    </section>

    <footer class="bg-sx-navy py-[28px]">
      <div class="home-footer-inner mx-auto flex w-full max-w-[1200px] flex-wrap items-center justify-between gap-[12px] px-[24px] text-[14px] text-[#8ea8c7]">
        <div class="flex items-center gap-[8px]">
          <img class="h-[22px]" :src="logoFan" :alt="platformName"/>
          <span v-if="hasText(platformName)">{{ platformName }}</span>
        </div>
        <div v-if="hasText(home.footerText)">{{ home.footerText }}</div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import logoFan from '@/assets/images/platform/logo-fan.svg'
import {getHomeConfig} from '@/api/platform'

const fallbackHomeConfig = {
  platformName: 'FAN · 凡集',
  heroBadge: '数据可视化 · 大屏搭建平台',
  heroTitle: 'FAN · 凡集',
  heroDescription: '一站式数据可视化展示平台，拖拽式自由布局、多数据源接入与实时更新，十分钟搭建专业数据大屏。',
  heroVideoUrl: '',
  heroPrimaryButtonText: '进入平台',
  heroSecondaryButtonText: '了解产品',
  introTitle: '让数据一眼被看见',
  introDescription: 'FAN · 凡集是一站式数据可视化展示平台，旨在帮助用户快速通过可视化图表展示大量数据，低门槛快速打造出专业大屏数据展示。精心预设多种行业模板，极致展示数据魅力。采用拖拽式自由布局，全图形化编辑，快速可视化制作。FAN · 凡集支持多种数据来源配置，支持数据实时同步更新，同时基于 Web 页面渲染，可灵活投屏多种屏幕终端。',
  introBackgroundUrl: 'https://qcloudimg.tencent-cloud.cn/trisys/assets/product/images/fTj6N6HCvoIdYGdjh9eF2.png',
  featureTitle: '产品特性',
  features: [
    {
      title: '酷炫大屏',
      image: 'https://qcloudimg.tencent-cloud.cn/trisys/assets/product/images/xvJ7ntosp7BrsXiqbGm5E.png',
      desc: '由专业设计师精心预设生产情况总览，政府工作指数，销售实时监控等多种行业模板，丰富的图形组件，使您可以通过图形化的界面在十分钟之内快速搭建起专业、美观、酷炫的大屏展示。'
    },
    {
      title: '自由布局',
      image: 'https://qcloudimg.tencent-cloud.cn/trisys/assets/product/images/qaagxLeAKs31tIiylVbnH.png',
      desc: '不必预先导入数据，用户可以直接将所要呈现的组件拖拽到画布上进行自由配置和布局，通过点击拖拽即可调整图层顺序，全程图形化编辑操作，所见即所得。'
    },
    {
      title: '灵活多端',
      image: 'https://qcloudimg.tencent-cloud.cn/trisys/assets/product/images/F0E8XtBLzoJO_5QCtVFNA.png',
      desc: '用户可按实际屏幕尺寸自定义任意画布大小，适配各种拼接大屏场景。大屏以网页形式预览和发布，页面基于 Web 渲染，无需额外安装软件，使用浏览器即可灵活投放至多种屏幕终端。'
    }
  ],
  contactTitle: '联系我们',
  contactDescription: '项目咨询与产品演示，欢迎随时联系',
  contactPhone: '156****7568',
  footerText: '© 2026 FAN · 凡集 · 保留所有权利'
}

const homeConfig = ref(fallbackHomeConfig)
const home = computed(() => ({
  ...fallbackHomeConfig,
  ...homeConfig.value,
  features: Array.isArray(homeConfig.value?.features) && homeConfig.value.features.length
      ? homeConfig.value.features
      : fallbackHomeConfig.features
}))
const platformName = computed(() => home.value.platformName)
const visibleFeatures = computed(() => (home.value.features || []).filter(item => {
  return hasText(item.title) || hasText(item.desc) || hasText(item.image)
}))
const introVisible = computed(() => {
  return hasText(home.value.introTitle) || hasText(home.value.introDescription) || hasText(home.value.introBackgroundUrl)
})
const featuresVisible = computed(() => {
  return hasText(home.value.featureTitle) || visibleFeatures.value.length > 0
})
const contactVisible = computed(() => {
  return hasText(home.value.contactTitle) || hasText(home.value.contactDescription) || hasText(home.value.contactPhone)
})
const heroActionsVisible = computed(() => {
  return hasText(home.value.heroPrimaryButtonText) || hasText(home.value.heroSecondaryButtonText)
})
const introStyle = computed(() => home.value.introBackgroundUrl ? {
  backgroundImage: `url("${home.value.introBackgroundUrl}")`
} : {})

function hasText(value) {
  return typeof value === 'string' && value.trim().length > 0
}

onMounted(async () => {
  try {
    homeConfig.value = await getHomeConfig()
  } catch (error) {
    // Keep the built-in content when the admin service is unavailable.
  }
})

const scrollToSection = (selector) => {
  document.querySelector(selector)?.scrollIntoView({behavior: 'smooth', block: 'start'})
}
</script>

<style scoped lang="scss">
.home-introduce-bg {
  background: url("https://qcloudimg.tencent-cloud.cn/trisys/assets/product/images/fTj6N6HCvoIdYGdjh9eF2.png") no-repeat left -10px;
}

.home-hero-desc,
.home-intro-desc,
.home-contact-desc {
  overflow-wrap: anywhere;
}

@media (max-width: 720px) {
  .home-header-inner {
    height: auto;
    min-height: 58px;
    flex-wrap: wrap;
    gap: 8px;
    padding: 10px 14px !important;
  }

  .home-header-inner > button:first-child {
    min-width: 0;
  }

  .home-header-inner .el-button {
    flex: none;
  }

  .home-brand-name {
    display: none;
  }

  .home-hero {
    height: auto;
    min-height: 490px;
    max-height: none;
    padding: 68px 0 58px;
  }

  .home-hero-inner,
  .home-intro-inner {
    padding-right: 16px !important;
    padding-left: 16px !important;
  }

  .home-hero-title {
    font-size: 33px;
    line-height: 44px;
  }

  .home-hero-desc,
  .home-intro-desc,
  .home-contact-desc {
    font-size: 15px;
    line-height: 26px;
  }

  .home-hero-desc {
    margin-top: 12px;
  }

  .home-hero-actions {
    margin-top: 22px;
  }

  .home-intro-inner {
    padding-top: 66px;
    padding-bottom: 66px;
  }

  .home-intro-title {
    font-size: 30px;
    line-height: 41px;
  }

  .home-features,
  .home-contact {
    padding-top: 66px;
    padding-bottom: 66px;
  }

  .home-section-title {
    margin-bottom: 28px;
    font-size: 25px;
    line-height: 35px;
  }

  .home-contact-phone {
    font-size: 25px;
    line-height: 36px;
  }

  .home-footer-inner {
    padding: 22px 16px;
  }
}
</style>
