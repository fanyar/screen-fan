<template>
<div>
  <canvas width="400" height="400" id="canvas" style="border: 1px solid #ccc;"></canvas>

  <div>x: {{ canvasOptions?.pointer?.x.toFixed(2) }}</div>
  <div>y: {{ canvasOptions?.pointer?.y.toFixed(2) }}</div>
</div>
</template>


<script setup>
import { onBeforeUnmount, onMounted, ref} from "vue";
import {fabric} from "fabric";

let canvasOptions = ref({})
const fabriccanvas = ref(null)
const init = () => {
  let cDom = document.getElementById('canvas')
  fabriccanvas.value = new fabric.Canvas(cDom);

// 创建一个矩形对象
  let rect = new fabric.Rect({
    id: '333',
    left: 100,   // 距离画布左边的距离
    top: 100,    // 距离画布顶部的距离
    fill: 'red', // 矩形的填充颜色
    width: 100,  // 矩形的宽度
    height: 100, // 矩形的高度
    selectable: false // 是否可选
  });

  fabriccanvas.value.add(rect);

  rect.on('mousedown', options => {
    console.log('矩形被点击释放', options);

    // 在这里可以执行任何你需要的操作
  });

  fabriccanvas.value.on('mouse:down', (options) => {
    if (options.target === rect) {
      console.log('矩形上的鼠标释放事件');
    }
    canvasOptions.value = options;
    console.log('fabriccanvas', options)
  });
}


onMounted(() => {
  init();
})
onBeforeUnmount(() => {
  if(fabriccanvas.value){
    fabriccanvas.value.dispose()
    fabriccanvas.value = null;
  }
})
</script>
<style scoped>

</style>
