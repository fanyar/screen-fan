<template>
  <div class="suzhou-garden-page">
    <SuzhouGardenTour
        :initial-view="initialView"
        :show-ui="showUi"
    />
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute} from 'vue-router'
import SuzhouGardenTour from '@/components/model/SuzhouGardenTour.vue'
import {SUZHOU_GARDEN_TOUR_ORDER} from '@/const/suzhou-garden'

const route = useRoute()
const initialView = computed(() => {
  const view = String(route.query.view || 'overview')
  return SUZHOU_GARDEN_TOUR_ORDER.includes(view) ? view : 'overview'
})
const showUi = computed(() => route.query.chrome !== '0')
</script>

<style scoped lang="scss">
.suzhou-garden-page {
  min-height: calc(100vh - 112px);
}
</style>
