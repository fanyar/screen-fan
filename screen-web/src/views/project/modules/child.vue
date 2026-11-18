<template>
<div class="child">
  <h2>这是子组件</h2>
  <div>{{ a }}</div>
  {{ book }}
  <el-button @click="changeA">子级修改数据</el-button>
  <el-button @click="changeA2($parent)">第二张子级修改数据</el-button>
</div>
</template>

<script setup name="child">
import {ref} from 'vue'


defineProps(['a'])
const emit = defineEmits(['changeA'])

function changeA(){
  emit('changeA', 10)
}

function changeA2(parent){
  parent.a = 500
}

let book = ref('jojo')


defineExpose({ book })
</script>

<style scoped lang="scss">
.child{
  background-color: skyblue;
}
</style>
