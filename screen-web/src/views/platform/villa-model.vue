<template>
  <div class="villa-model-page">
    <VillaTour :initial-view="'overview'"/>
  </div>
</template>

<script setup>
import VillaTour from '@/components/model/VillaTour.vue'
</script>

<style scoped lang="scss">
.villa-model-page {
  min-height: calc(100vh - 112px);
}
</style>
