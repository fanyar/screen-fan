<template>
  <!--  <div @click="$router.push('/home')">welcome</div>-->

  <div
    class="grid h-full min-h-screen w-screen min-w-full grid-cols-[clamp(0px,(100%-800px)*999,200px)_1fr] grid-rows-[100px_1fr_100px] overflow-hidden text-[14px] [grid-template-areas:'header_header'_'menu_content'_'footer_footer']"
  >
    <div class="flex cursor-pointer items-center justify-center border border-[#ccc] bg-[#eee] [grid-area:header]">
      <div class="text-[24px] font-medium" @click="$router.push('/home')">welcome</div>
    </div>
    <div class="border border-[#ccc] bg-red-500 [grid-area:menu]"></div>
    <div
      class="grid h-[calc(100vh-202px)] content-start gap-[20px] overflow-y-auto overflow-x-hidden border border-[#ccc] bg-[#eee] p-[20px] [grid-area:content] [grid-template-columns:repeat(auto-fill,minmax(200px,1fr))]"
    >
      <div
        class="flex h-[200px] items-center justify-center border border-[#333] bg-[#ccc] text-[14px]"
        v-for="n in 10"
        :key="n"
      >
        {{ n }}
      </div>
    </div>
    <div class="border border-[#ccc] bg-[#eee] [grid-area:footer]"></div>
  </div>

</template>

<script setup>
import {onMounted} from "vue";
import {hospitalPage} from "../../api/home.js";

const init = async () => {
  let res = await hospitalPage({sysCode: "fab"});
  console.log(res);
}

onMounted(() => {
  init()
})
</script>
