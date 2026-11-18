<template>
  <div class="login-warp">
    <BorderBox11 title="登 录" :color="['#3171c2', 'rgba(49,113,194 ,0.5)']" class="login-box">
      <el-form
          ref="ruleFormRef"
          :model="ruleForm"
          :rules="rules"
          class="login-form"
      >
        <el-form-item label="账号" prop="name">
          <el-input v-model="ruleForm.name" placeholder="请输入账号"/>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="ruleForm.password" type="password" placeholder="请输入密码"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm(ruleFormRef)">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </BorderBox11>
  </div>
</template>

<script setup>
import {ref, reactive} from "vue"
import {useRouter} from "vue-router"
import {BorderBox11, Button} from '@kjgl77/datav-vue3'
import {setStore} from "@/util/store"

const router = useRouter();
const validatorPwd = (rule, value, callback) => {
  if (!value) {
    callback("请输入密码");
  } else {
    let reg = /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{3,8}$/;
    if (!reg.test(value)) {
      callback("密码包含字母数字， 长度为3-8位");
    } else {
      callback()
    }
  }
}

let ruleFormRef = ref();
let ruleForm = reactive({
  name: "",
  password: ""
});
let rules = ref({
  name: [
    {required: true, message: '请输入账号', trigger: 'blur'},
    {min: 2, max: 8, message: '2~8个字符', trigger: 'blur'},
  ],
  password: [{required: true, validator: validatorPwd}]
})

const submitForm = async (formEl) => {
  if (!formEl) return
  const valid = await formEl.validate();
  if (valid) {
    setStore({
      name: "userInfo",
      content: {
        name: ruleForm.name,
        password: ruleForm.password
      }
    })
    await router.push({path: "/home"})
  }
}

</script>

<style scoped lang="scss">
.login-warp {
  width: 100%;
  height: 100%;
  background: url("@/assets/images/login-bg.png") no-repeat left top;
  background-size: 100% 100%;

  .login-box {
    width: 450px;
    height: 300px;
    position: absolute;
    right: 10%;
    top: 50%;
    margin-top: -150px;

    .login-form {
      margin-top: 100px;

      :deep(.el-form-item) {
        margin-top: 30px;
        text-align: center;

        .el-form-item__label {
          color: #fff;
          margin-left: 50px;
        }

        .el-input {
          width: 300px;

          .el-input__wrapper {
            background-color: rgba(255, 255, 255, 0.5);

            .el-input__inner {
              color: #fff;

              &::placeholder {
                color: #fff; /* 修改为你想要的颜色 */
                opacity: 1; /* 防止某些浏览器默认半透明 */
              }
            }
          }
        }
      }

      :deep(.el-button) {
        width: 100px;
        text-align: center;
        margin: 0 auto;
      }
    }
  }
}

</style>
