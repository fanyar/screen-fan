<template>
  <div>车的信息：品牌{{car.name}},价格{{car.price}}</div>
  <el-button @click="changeCar">修改车</el-button>
  <el-button @click="changePrice">修改价格</el-button>
  <div>computed: {{obj}}</div>

  <hr/>
  <div>{{ person.car.c1 }}, {{ person.car.c2 }}</div>
  <el-button @click="changeCarName">修改车主</el-button>
  <el-button @click="changeC1">修改第1台车</el-button>
  <el-button @click="changeC2">修改第2台车</el-button>
  <el-button @click="changeAllCar">修改全部车</el-button>
  <hr/>
  <div>{{ shu }}, {{ shu2.name }}</div>
  <el-button @click="changeshu">修改shu</el-button>
  <el-button @click="changeshu2">修改shu2</el-button>

  <br/>
  <div>{{ p.name }}</div>
  <el-button @click="p.name += '@' ">watch p</el-button>

  <br/>
  <div>pp的数据</div>
  <div>{{ pp.info.abc.ww }}</div>
  <el-button @click="editWW">结构- 修改pp.info.abc.ww的数据</el-button>


</template>

<script setup>
  import { ref, reactive, computed , watch, toRefs} from 'vue'
  let car = reactive({ name: '奔驰', price: 100 });

  const changeCar = () => {
    Object.assign(car, {name: '比亚迪', price: 33})
  }
  const changePrice = () =>{
    car.price += 10
  }

  let obj = computed(() => {
    return car.name + '-' + car.price
  })




  let person = reactive({
    name: 'aaa',
    age: 15,
    car: {
      c1: '大众',
      c2: '本田'
    }
  })
  const changeCarName = () => {
    person.name = 'wwwww'
  }
  const changeC1 = () => {
    person.car.c1 = '奔驰'
  }
  const changeC2 = () => {
    person.car.c2 = '宝马'
  }
  const changeAllCar = () => {
    person.car = {
      c1: '劳斯莱斯',
      c2: '玛莎拉蒂'
    }
  }

  watch(() => person.car, (val) => {
    console.log('111',val)
  },{ deep: true })
  watch([() => person.name ,() => person.car.c1], (val) => {
    console.log('2222',val)
  },{ deep: true })


  let shu = ref('语文');
  let shu2 = ref({ name: '数学', name2: '英语' });
  const changeshu = () => {
    shu.value += '~'
  }
  const changeshu2 = () => {
    shu2.value.name += '*'
  }

  watch(shu, (val) => {
    console.log(val)
  })
  watch(() => shu2.name2, (val) => {
    console.log(val)
  }, { deep: true })



  let p = reactive({
    name: 'aa'
  })
  watch(p, (n,o) => {
    console.log(n,o)
  })


  let pp = reactive({
    name: 'aaa',
    info: {
      abc: {
        ww: "QQ"
      }
    }
  })

  const editWW = () => {
    let { name, info } = toRefs(pp);
    info.value.abc.ww = '789'
  }

</script>

<style scoped>

</style>
