<template>
  <el-dialog
      v-model="dialogVisibleCustom"
      :close-on-click-modal="false"
      title="管理自定义大屏"
      width="520px"
  >
    <div class="custom-menu scrollBar">
      <div class="item" v-for="(item,idx) in customMenuList" :key="item.id">
        {{item.name}}
        <i class="icon-Delete" @click="removeMenu(item,idx)"></i>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="saveCustomCreenMenu" class="beam-btn">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: "manageLargeScreenMenus",
  data(){
    return {
      dialogVisibleCustom: false,
      removeMenus: [],
    }
  },
  props:{
    customMenuList: {
      type: Array,
      default(){
        return []
      }
    }
  },
  methods:{
    show(){
      this.dialogVisibleCustom = true;
      this.removeMenus = [];
    },
    removeMenu(item,i){
      this.removeMenus.push(item);
      this.customMenuList.splice(i,1);
    },
    saveCustomCreenMenu(){
      let removeMenuIds = [];
      this.removeMenus.forEach(opt => {
        removeMenuIds.push(opt.id)
      })
      this.dialogVisibleCustom = false;
      this.removeMenus.forEach(opt => {
        let obj = {
          label: '大屏*',
          meat: { label: opt.name },
          params: { screenName: opt.id },
          query: {},
          value: opt.path
        }
        // this.$store.commit('DEL_TAG', obj)
      })
      /*delFabcustomscreen({ids: removeMenuIds.join(',')}).then(res => {
        if(res.data.data){
          this.$message.success('操作成功');
          this.$store.commit('SET_UPDATECUSTOMSCREENMENU',true)
        }
      })*/
    },
  }
}
</script>

<style scoped lang="scss">
.custom-menu{
  height: 350px;
  overflow: auto;
  .item{
    height: 48px;
    line-height: 48px;
    background: #F5F3FF;
    border-radius: 4px;
    font-weight: 400;
    font-size: 14px;
    color: rgba(0,0,0,0.9);
    margin-bottom: 12px;
    padding: 0 12px;

    &:last-child{
      margin-bottom: 0;
    }
    i{
      float: right;
      cursor: pointer;
    }
  }
}
</style>
