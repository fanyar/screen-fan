<template>

  <div style="position: relative; margin-left: 50px">
    <div class="big-run-way" :style="{ width: width+'px', height: height+'px' }">
      <div class="small-run-way"></div>
<!--      <div class="ball a" :style="{left: initX+'px', top: initY+'px'}"></div>-->
      {{ballList}}
      <div class="ball" v-for="item in ballList" :style="{left: item.left+'px', top: item.top+'px'}"></div>
    </div>
  </div>

</template>

<script setup>
import {ref, reactive, onUnmounted} from 'vue'
    let width =  ref(500);
    let height = ref(300);
    let initX = ref(500);
    let initY = ref(150);
    let timer =  null;
    let ballList = reactive([
      { length: 0, }
    ]);
    timer = setInterval(() => {
      ballList[0].length += 1;
      init(ballList)
    },30)

    const init = (ballList) => {
      ballList.forEach(opt => {
        let left = 0;
        let top = 0;
        let len = opt.length % (width.value*2 + height.value*2)
        opt['len'] = len

        if(len >= 1450){
          top = len - width.value*2-height.value-initY.value;
          left = initX.value
        }else if(len >= 950){
          top = 0;
          left = len - width.value-height.value-initY.value
        }else if(len >= 650){
          left = 0;
          top = height.value - (len - width.value -initY.value);
        }else if(len >= 150){
          top = height.value;
          left = width.value - (len - initY.value)
        }else{
          top = len + initY.value;
          left = initX.value;
        }

        opt['left'] = left
        opt['top'] = top
      })
    }

    onUnmounted(() => {
      clearInterval(timer)
    })
</script>

<style scoped lang="scss">
.big-run-way{
  border: 1px solid #000;
  border-radius: 20px;
}
.small-run-way{
  width: calc(100% - 50px);
  height: calc(100% - 50px);
  border: 1px solid #000;
  border-radius: 20px;
  margin: 25px;
}
.ball{
  width: 20px;
  height: 20px;
  border-radius: 20px;
  background: red;
  position: absolute;
}
</style>
