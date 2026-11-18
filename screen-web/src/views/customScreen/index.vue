<template>
  <!-- 自定义大屏  -->
  <el-container>
    <el-main>
      <div class="steps-wrap">
        <div class="left">
          <div class="name">全部组件</div>
          <div class="module-name" @click="showModule = !showModule">
            <el-icon v-if="showModule"><CaretBottom /></el-icon>
            <el-icon v-else><CaretTop /></el-icon>
            模块名称
          </div>
          <div class="left-module" v-show="showModule">
            <draggable
                class="module-list"
                :list="moduleList"
                animation="300"
                :forceFallback="true"
                :scroll-sensitivity="150"
                itemKey="name"
                :group="{ name: 'viewModules', pull: 'clone', put: false }"
                :clone="cloneModule"
            >
              <template #item="{ element, index }">
                <div class="module-item">
                  <img :src="getImg(element.imgName)" />
                  <div>{{ element.name }}</div>
                  <el-icon v-if="element.flag"><CircleCheckFilled /></el-icon>
                </div>
              </template>
            </draggable>
          </div>
        </div>
        <div class="right">
          <div class="module-info">
            <div class="name">通用设置</div>
            <el-button type="primary" style="float: right" @click="openCustomMenu">管理自定义大屏</el-button>
            <el-form ref="formScreen" :inline="true" :model="formScreen">
              <el-form-item label="放置位置:">
                <el-select v-model="formScreen.parentMenu" placeholder="请选择">
                  <el-option :label="item.name" :value="item.id" v-for="item in screenPoslist"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="自定义名称:">
                <el-input v-model="formScreen.name" placeholder="请输入" type="text" maxlength="20"></el-input>
              </el-form-item>
            </el-form>
          </div>
          <div class="module-box scrollBar">
            <div class="top">
              <div class="name">页面排版</div>
              <div>
                <el-button class="beam-btn-def" @click="previewScreen">预览</el-button>
                <!--            <el-button class="beam-btn-def" @click="confidentialityHandle">生成免密链接</el-button>-->
                <!--            <el-button class="beam-btn" @click="saveCustomScreen">保存</el-button>-->
              </div>
            </div>
            <div class="right-box">
              <div class="top-title" v-show="formScreen.hasTitle">
                <div>
                  顶部标题
                </div>
              </div>
              <div class="right-module">
                <draggable
                    class="module-list-right"
                    :list="moduleListView"
                    animation="300"
                    :forceFallback="true"
                    :scroll-sensitivity="150"
                    group="viewModules"
                    itemKey="name"
                    draggable=".item"
                    ghost-class="ghost"
                    @add="addModuleHandle"
                >
                  <template #item="{ element, index }">
                    <div class="module-item" :style="setViewModule(element)" v-if="moduleListView.length">
                      <div class="module">{{ element.name }}</div>
                      <el-icon @click="delModuleHandle(element)"><CircleCloseFilled /></el-icon>
                    </div>
                  </template>
                </draggable>

                <!--    图例      -->
                <!--            <div class="module-legend-r" v-if="moduleList[7].flag"></div>-->
                <!--            <div class="module-legend-b" v-if="moduleList[21].flag"></div>-->

                <!--    辅助线    -->
                <div class="row1"></div>
                <div class="row2"></div>
                <div class="col1"></div>
                <div class="col2"></div>
                <div class="col3"></div>
              </div>
            </div>
          </div>
        </div>

        <!--  自定义大屏菜单  -->
        <manage-large-screen-menus ref="manageLargeScreenMenus" :customMenuList="customMenuList"></manage-large-screen-menus>
      </div>
    </el-main>
  </el-container>
</template>

<script>
import draggable from 'vuedraggable'

// import {addModule,delModule} from './bs-custom.js'
import {addModule,delModule} from 'bs-custom'
import ManageLargeScreenMenus from "./modules/manageLargeScreenMenus.vue";
import {MODULE_LIST} from "@/const/customModules.js"

export default {
  name: "index",
  components: {
    draggable, ManageLargeScreenMenus
  },
  data(){
    return {
      showModule: true,
      formScreen: {
        parentMenu: '',
        name: '',
        hasTitle: true,
        hasBackground: true
      },
      moduleList: MODULE_LIST.map(item => ({...item, flag: false})),
      moduleListView: [],
      rightModulePos: [
        { top: 110, left: 150, flag: false },
        { top: 110, left: 450, flag: false },
        { top: 110, left: 750, flag: false },
        { top: 110, left: 1050, flag: false },
        { top: 330, left: 150, flag: false },
        { top: 330, left: 450, flag: false },
        { top: 330, left: 750, flag: false },
        { top: 330, left: 1050, flag: false },
        { top: 550, left: 150, flag: false },
        { top: 550, left: 450, flag: false },
        { top: 550, left: 750, flag: false },
        { top: 550, left: 1050, flag: false },
      ],
      moduleItemW: 300,   //12宫格 某一宫格的宽度
      moduleItemH: 220,
      moduleX: 0,         //距离左边的距离
      moduleY: 0,
      currentModule: {},

      dialogVisible: false,
      ruleForm: {
        time: 1,
        beamName: '',
        link: ''
      },
      screenPoslist: [],
      beamList: [],
      customMenuList: [],
      removeMenus: []
    }
  },
  created() {

  },
  methods:{
    getImg(imgName){
      let imgUrl = new URL(`/src/assets/screen/module/${imgName}.png`, import.meta.url).href;
      return imgUrl;
    },
    cloneModule(val){
      this.currentModule = val;
      return val
    },
    previewScreen(){
      if(!this.moduleListView.length){
        this.$message.warning('请拖入组件');
        return false;
      }

      this.$router.push('/test/screen/customView');

      this.$store.commit('SET_CUSTOMSCREEN',{
        formScreen: this.formScreen,
        moduleListView: this.moduleListView
      })
    },
    confidentialityHandle(){
      this.dialogVisible = true;
      this.$nextTick(() => {
        this.$refs['ruleFormRef'].resetFields()
      })
    },
    async saveCustomScreen(){
      if(!this.formScreen.name.trim()){
        this.$message.warning('请输入自定义名称');
        return false;
      }
      if(!this.moduleListView.length){
        this.$message.warning('请拖入组件');
        return false;
      }
      let list = [];
      this.moduleListView.forEach(opt => {
        let {index,left,moduleId,moduleName,name,seatPos,top,x,y} = opt;
        list.push({
          moduleId,
          moduleCode: moduleName,
          moduleName: name,
          imgname: '',
          moduleIndex: index,
          seatPos,
          posTop: top,
          posLeft: left,
          posRows: y,
          posColumns: x,
          flag: ''
        })
      })
      let _formScreen = JSON.parse(JSON.stringify(this.formScreen));
      _formScreen['hasBackground'] =  _formScreen['hasBackground'] ? 1 : 0;
      _formScreen['hasTitle'] =  _formScreen['hasTitle'] ? 1 : 0;
      let obj = {
        customScreenInfoVO: _formScreen,
        customScreenModuleInfoVOList: list
      };
      /*let res = await saveFabcustomscreen(obj);
      if(!res.data.code){
        this.$message.success('操作成功');
        this.resetCustom();
        this.$store.commit('SET_UPDATECUSTOMSCREENMENU',true)
      }else{
        this.$message.warning(res.data.msg)
      }*/
    },
    setViewModule(data){
      return {
        'width': this.moduleItemW * data.x +'px',
        'height': this.moduleItemH * data.y + 'px',
        'top': data.top - this.moduleItemH/2 + 'px',
        'left': data.left - this.moduleItemW/2 + 'px',
      }
    },
    addModuleHandle(ev){
      /* 判断组件关联关系 */
      if(this.currentModule.parentId){
        let idx = this.moduleListView.findIndex(opt => opt.moduleId == this.currentModule.parentId);
        if(idx == -1){
          let item = this.moduleList.find(opt => opt.moduleId == this.currentModule.parentId);
          this.$message.warning(`请先选择${ item.name }组件`);
          this.moduleListView.pop();
          return;
        }
      }

      let data = addModule(ev,this.moduleItemW, this.moduleItemH,this.currentModule, this.moduleListView, this.rightModulePos);
      if(data.msg){
        this.$message.warning(data.msg);
        this.moduleListView = data.moduleListView;
        return false;
      }else{
        let { top,left,idx,seatPos } = data;
        this.currentModule['flag'] = true;
        this.moduleListView[this.moduleListView.length-1]['top'] = top;
        this.moduleListView[this.moduleListView.length-1]['left'] = left;
        this.moduleListView[this.moduleListView.length-1]['index'] = idx;
        this.moduleListView[this.moduleListView.length-1]['seatPos'] = seatPos.join(',');
      }

      console.log('right-add',this.moduleListView)
    },
    delModuleHandle(data){
      let _data = delModule(data, this.moduleListView, this.moduleList, this.rightModulePos);
      let {moduleListView, rightModulePos} = _data;
      this.moduleListView = moduleListView;
      this.rightModulePos = rightModulePos;

      console.log('del',this.moduleListView,this.rightModulePos)
    },
    /*addModuleHandle(ev){
      let idx = this.findNearest(ev);
      if(idx === false){
        this.warnHandle('超出预览范围，请重新拖拽');
        return false;
      }
      let items = this.moduleListView.filter(opt => opt.name == this.currentModule.name);
      if(items.length > 1){
        this.warnHandle('组件重复，请重新拖拽');
        return false;
      }

      let {top, left} = this.rightModulePos[idx];
      let {x,y} = this.moduleListView[this.moduleListView.length-1];
      let h = top + (y - 1) * this.moduleItemH;
      if(h > this.moduleItemH * 3){
        this.warnHandle('当前位置不够，请重新选择');
        return false;
      }
      let w = left + (x - 1) * this.moduleItemW;
      if(w > this.moduleItemW * 4){
        this.warnHandle('当前位置不够，请重新选择');
        return false;
      }
      let seatPos = [];
      for(let i = 0; i < y; i++){
        seatPos.push(idx + 4*i)
      }
      if(x == 1 || y == 1){
        for(let i = 0; i < x; i++){
          seatPos.push(idx + i)
        }
      }else{
        for(let i = 0; i < x; i++){
          seatPos.push(seatPos[i]+1)
        }
      }
      seatPos = Array.from(new Set(seatPos));

      let flagSeat = false;
      for(let i = 0; i < seatPos.length; i++){
        let _i = seatPos[i];
        if(this.rightModulePos[_i]['flag']){
          flagSeat = true
          break;
        }else{
          this.rightModulePos[_i]['flag'] = true;
        }
      }
      if(flagSeat){
        this.warnHandle('当前位置已有组件，请重新选择');
        return;
      }

      //图例--项目桥梁列表[3,7,11]  各桥梁架设情况[9,10]
      let legendR = [];
      if(this.currentModule.moduleId === '10007'){
        legendR = [3,7,11];
      }else if(this.currentModule.moduleId === '10021'){
        legendR = [9,10];
      }
      legendR.forEach(o => {
        this.rightModulePos[o]['flag'] = true;
      })

      this.currentModule['flag'] = true;
      this.moduleListView[this.moduleListView.length-1]['top'] = top;
      this.moduleListView[this.moduleListView.length-1]['left'] = left;
      this.moduleListView[this.moduleListView.length-1]['index'] = idx;
      this.moduleListView[this.moduleListView.length-1]['seatPos'] = seatPos.join(',');

      console.log(idx,'right-add',this.moduleListView)
    },
    delModuleHandle(data){
      let moduleId = data.moduleId;
      let idx = this.moduleListView.findIndex(opt => opt.moduleId == moduleId);
      let module = this.moduleList.find(opt => opt.moduleId == moduleId);
      this.moduleListView.splice(idx,1);
      module['flag'] = false;
      let arrIdx = data.seatPos.split(',');
      arrIdx.forEach(i => {
        this.rightModulePos[i]['flag'] = false;
      })

      //图例--项目桥梁列表[3,7,11]  各桥梁架设情况[9,10]
      let legendR = [];
      if(data.moduleId === '10007'){
        legendR = [3,7,11];
      }else if(data.moduleId === '10021'){
        legendR = [9,10];
      }
      legendR.forEach(o => {
        this.rightModulePos[o]['flag'] = false;
      })

      console.log(this.moduleListView,this.rightModulePos)
    },*/
    findNearest(ev){
      let x = ev.originalEvent.layerX;
      let y = ev.originalEvent.layerY;
      if(x > this.moduleItemW*4 || y > this.moduleItemH*3){
        return false;
      }

      let iMin = 999999;
      let iMinIndex = -1;
      this.rightModulePos.forEach((opt,idx) => {
        let a = opt.top - y;
        let b = opt.left - x;
        let dis = Math.sqrt(a*a+b*b);
        if(dis < iMin){
          iMin = dis;
          iMinIndex = idx
        }
      })

      if(iMinIndex == -1){
        return false
      }else{
        return iMinIndex;
      }
    },
    warnHandle(txt){
      this.$message.warning(txt);
      this.moduleListView.pop();
    },
    resetCustom(){
      this.init();
      this.formScreen.name = '';
      this.formScreen.hasTitle = true;
      this.formScreen.hasBackground = true;
      this.moduleListView = [];
      this.rightModulePos.forEach(opt => opt.flag = false);
      this.moduleList.forEach(opt => opt.flag = false);
    },
    openCustomMenu(){
      this.$refs.manageLargeScreenMenus.show();
      let customMenu = this.$store.state.screen.customScreenMenu;
      this.customMenuList = JSON.parse(JSON.stringify(customMenu));
    },
    removeMenu(item,i){
      this.removeMenus.push(item);
      this.customMenuList.splice(i,1);
    },
  }
}
</script>

<style scoped lang="scss">
.steps-wrap {
  display: flex;
  height: 100%;

  .left, .right {
    .name{
      font-size: 20px;
      font-weight: 600;
      color: rgba(0,0,0,0.9);
      margin-bottom: 20px;
    }
  }

  .left {
    width: 350px;
    padding: 20px;
    margin-right: 20px;
    border-radius: 12px;
    background-color: #fff;
    .module-name{
      margin-bottom: 20px;
      cursor: pointer;
      user-select: none;
      i{
        color: #B2B2B2;
        vertical-align: middle;
        margin-right: 4px;
      }
      font-weight: 400;
      font-size: 14px;
      color: rgba(0,0,0,0.9);
    }
    .left-module{
      height: calc(100% - 90px);
      flex-shrink: 0;
      overflow: auto;

      .module-list{
        overflow: hidden;
        .module-item{
          width: 150px;
          float: left;
          margin-right: 20px;
          margin-bottom: 20px;
          text-align: center;
          font-weight: 400;
          font-size: 14px;
          color: rgba(0,0,0,0.7);
          cursor: pointer;
          position: relative;
          &:nth-child(2n){
            margin-right: 0;
          }
          img{
            width: 100%;
            height: 84px;
            margin-bottom: 4px;
          }
          i{
            color: #338300;
            position: absolute;
            right: -6px;
            top: 70px;
          }
        }
      }
    }
  }
  .right {
    flex: 1;
    overflow: hidden;
    .module-info, .module-box{
      padding: 20px;
      background: #fff;
      border-radius: 12px;
    }
    .module-info{
      padding-bottom: 0;
      position: relative;

      .custom-btn{
        position: absolute;
        right: 20px;
        top: 20px;
        font-weight: 400;
        font-size: 14px;
        color: #5B42FF;
        cursor: pointer;

        i{
          margin-right: 4px;
          vertical-align: middle;
        }
      }
    }
    .module-box{
      margin-top: 20px;
      height: calc(100% - 178px);
      min-height: 580px;
      overflow: auto;
      .top{
        display: flex;
        justify-content: space-between;
      }
      .right-box{
        width: 1206px;
        border: 1px dashed #D2D0DB;
        .top-title{
          font-weight: 400;
          font-size: 14px;
          color: rgba(0,0,0,0.9);
          text-align: center;
          border-bottom: 1px dashed #D2D0DB;
          position: relative;
          div{
            margin: 4px;
            padding: 2px 0;
            background: #E9E7ED;
          }
          span{
            position: absolute;
            right: 16px;
            &:before{
              content: '';
              width: 16px;
              height: 16px;
              background: #C6C4D1;
              display: inline-block;
              margin-top: 2px;
              margin-right: 4px;
              vertical-align: text-bottom;
            }
          }
        }
      }
    }
    .right-module{
      width: 1200px;
      height: 660px;
      //padding: 2px;
      position: relative;
      overflow: hidden;
      .module-list-right{
        width: 100%;
        height: 100%;

        .module-item{
          position: absolute;
          pointer-events: none;
          z-index: 3;
          .module{
            width: calc(100% - 10px);
            height: calc(100% - 10px);
            background: #C4BBFF;
            margin: 5px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 400;
            font-size: 14px;
            color: rgba(0,0,0,0.9);
          }
          i{
            position: absolute;
            top: 10px;
            right: 10px;
            color: #E9E7ED;
            cursor: pointer;
            z-index: 6;
            pointer-events: auto;
          }
        }
      }
      .ghost{
        opacity: 0;
      }

      .module-legend-r , .module-legend-b{
        background: #E9E7ED;
        margin: 5px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 400;
        font-size: 14px;
        color: rgba(0, 0, 0, 0.9);
      }
      .module-legend-r{
        width: 290px;
        height: 650px;
        float: right;
        margin-top: -655px;
      }
      .module-legend-b{
        width: 590px;
        height: 210px;
        float: left;
        margin-top: -215px;
        margin-left: 305px;
      }

      .col1, .col2, .col3{
        position: absolute;
        height: 100%;
        width: 1px;
        border-right:1px dashed #D2D0DB;
      }
      .row1, .row2{
        position: absolute;
        width: 100%;
        height: 1px;
        border-top:1px dashed #D2D0DB;
      }
      .col1{
        top: 0;
        left: 300px;
      }
      .col2{
        top: 0;
        left: 600px;
      }
      .col3{
        top: 0;
        left: 900px;
      }
      .row1{
        top: 220px;
        left: 0;
      }
      .row2{
        top: 440px;
        left: 0;
      }
    }
  }

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

}


</style>
