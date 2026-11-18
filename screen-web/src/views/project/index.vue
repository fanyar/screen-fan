<template>
  <div>
    <div class="textautofit" style="font-size: 40px;">project</div>
    <div>name: {{ userStore.userName }}</div>
    <div>pwd: {{ userStore.password }}</div>
    <el-button @click="editAdmin">修改admin</el-button>
    <el-button @click="editUser">修改user-批量修改数据</el-button>

    <el-button @click="editChild">父级改变子级的数据{{ a }}</el-button>
    <el-button @click="getChild($refs)">获取所有子级</el-button>
    <child ref="c1" :a="a" @changeA="changeValue"/>


    <canvas id="canvas" width="400" height="400"></canvas>

    <drawPoint></drawPoint>

  </div>
</template>

<script setup>
import drawRect from "./drawRect.vue"
import drawPoint from "./drawPoint.vue"

import {ref, reactive, onMounted, onUnmounted} from 'vue'
import Child from "./modules/child.vue";
import {useUserStore, useCommonStore} from "@/store"

const userStore = useUserStore();
const commonStore = useCommonStore();

const editAdmin = () => {
  //修改数据第一种方式
  userStore.userName = 'jojo'
}
const editUser = () => {
  //修改数据第二种方式
  /*userStore.$patch({
    userName: 'aaa',
    password: '789'
  })*/
  // 修改数据第三种方式
  userStore.editInfo(555)
}

let a = ref(150); // 椭圆的宽度
let b = ref(100); // 椭圆的高度
let lengthes = reactive([0, 100, 200, 300, 350]); // 小球移动的长度
let timer = null;


defineExpose({a});

let c1 = ref();
const editChild = () => {
  c1.value.book = '语文'
}
const getChild = value => {
  console.log(value);
}

const changeValue = value => {
  a.value += value
}

const main = () => {
  const canvas = document.getElementById('canvas');
  const ctx = canvas.getContext('2d');

  clearCanvas(ctx, canvas);
  drawEllipse(ctx, canvas);
  createAndMoveBalls(ctx, canvas);
}
// 绘制椭圆
const drawEllipse = (ctx, canvas) => {
  for (let i = 0; i < 360; i++) {
    const x = a.value * Math.cos(i * (Math.PI / 180));
    const y = b.value * Math.sin(i * (Math.PI / 180));
    ctx.beginPath();
    ctx.arc(x + canvas.width / 2, y + canvas.height / 2, 5, 0, 2 * Math.PI);
    ctx.fillStyle = '#ccc';
    ctx.fill();
  }
}
// 创建并移动小球
const createAndMoveBalls = (ctx, canvas) => {
  for (let j = 0; j < lengthes.length; j++) {
    let angle = 0;
    const length = lengthes[j];
    angle = length;
    const newX = a.value * Math.cos(angle * (Math.PI / 180));
    const newY = b.value * Math.sin(angle * (Math.PI / 180));
    ctx.beginPath();
    ctx.arc(newX + canvas.width / 2, newY + canvas.height / 2, 10, 0, 2 * Math.PI);
    ctx.fillStyle = '#2f83d5';
    ctx.fill();
  }
}
const clearCanvas = (ctx, canvas) => {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
}

onMounted(() => {
  let n = 0
  timer = setInterval(() => {
    n += 10;
    // console.log(n, n % 360)
    lengthes = [n];
    main()
  }, 1000)
})
onUnmounted(() => {
  clearInterval(timer)
})


</script>

<style scoped lang="scss">

</style>
