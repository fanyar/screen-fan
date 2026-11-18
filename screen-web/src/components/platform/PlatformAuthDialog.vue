<template>
  <el-dialog
      v-model="visible"
      class="platform-auth-dialog"
      width="420px"
      align-center
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
  >
    <template #header>
      <div class="mb-[4px]">
        <div class="text-[22px] font-semibold leading-[30px] text-sx-title">{{ dialogTitle }}</div>
        <div class="mt-[4px] text-[16px] leading-[24px] text-sx-muted">欢迎来到 {{ platformShortName }}</div>
      </div>
    </template>

    <div class="mb-[20px] flex rounded-[4px] bg-sx-canvas p-[4px]">
      <button
          v-for="item in authModes"
          :key="item.value"
          type="button"
          class="h-[34px] flex-1 rounded-[4px] text-[15px] transition-colors"
          :class="mode === item.value ? 'bg-sx-surface font-medium text-sx-primary shadow-[0_1px_4px_rgba(15,23,42,0.08)]' : 'text-sx-muted hover:text-sx-title'"
          @click="switchMode(item.value)"
      >
        {{ item.label }}
      </button>
    </div>

    <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        size="large"
        @submit.prevent="handleSubmit"
    >
      <el-form-item label="账号" prop="account">
        <el-input v-model.trim="formData.account" placeholder="请输入账号" maxlength="20"/>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
            v-model.trim="formData.password"
            type="password"
            show-password
            placeholder="请输入密码"
            maxlength="18"
        />
      </el-form-item>
      <el-form-item v-if="isRegisterMode" label="确认密码" prop="confirmPassword">
        <el-input
            v-model.trim="formData.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入密码"
            maxlength="18"
        />
      </el-form-item>
      <el-button
          class="!mt-[4px] !h-[44px] !w-full"
          type="primary"
          native-type="submit"
          :loading="submitting"
      >
        {{ isRegisterMode ? '注 册' : '登 录' }}
      </el-button>
    </el-form>
  </el-dialog>
</template>

<script setup>
import {computed, nextTick, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {PLATFORM_AUTH_MODES, PLATFORM_SHORT_NAME} from '@/const/platform'
import {useUserStore} from '@/store/modules/user'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String,
    default: PLATFORM_AUTH_MODES.LOGIN,
    validator: (value) => Object.values(PLATFORM_AUTH_MODES).includes(value)
  }
})

const emit = defineEmits(['update:modelValue', 'mode-change', 'success'])

const platformShortName = PLATFORM_SHORT_NAME

const authModes = [
  {label: '登录', value: PLATFORM_AUTH_MODES.LOGIN},
  {label: '注册', value: PLATFORM_AUTH_MODES.REGISTER}
]

const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)
const formData = reactive({
  account: '',
  password: '',
  confirmPassword: ''
})

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})
const isRegisterMode = computed(() => props.mode === PLATFORM_AUTH_MODES.REGISTER)
const dialogTitle = computed(() => isRegisterMode.value ? '创建账号' : '账号登录')

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback('请输入密码')
    return
  }
  if (!/^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{6,18}$/.test(value)) {
    callback('密码需包含字母和数字，长度为6-18位')
    return
  }
  callback()
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback('请再次输入密码')
    return
  }
  if (value !== formData.password) {
    callback('两次输入的密码不一致')
    return
  }
  callback()
}

const formRules = computed(() => ({
  account: [
    {required: true, message: '请输入账号', trigger: 'blur'},
    {min: 2, max: 20, message: '账号长度为2-20位', trigger: 'blur'}
  ],
  password: [{required: true, validator: validatePassword, trigger: 'blur'}],
  confirmPassword: isRegisterMode.value
    ? [{required: true, validator: validateConfirmPassword, trigger: 'blur'}]
    : []
}))

const switchMode = (mode) => {
  if (props.mode === mode) return
  emit('mode-change', mode)
}

const resetForm = () => {
  formData.account = ''
  formData.password = ''
  formData.confirmPassword = ''
  nextTick(() => formRef.value?.clearValidate())
}

const handleSubmit = async () => {
  if (submitting.value) return

  try {
    await formRef.value?.validate()
  } catch (error) {
    return
  }

  submitting.value = true
  try {
    const userInfo = {name: formData.account}
    userStore.setUserInfo(userInfo)
    ElMessage.success(isRegisterMode.value ? '注册成功' : '登录成功')
    emit('success', userInfo)
    visible.value = false
  } catch (error) {
    ElMessage.error(isRegisterMode.value ? '注册失败，请稍后重试' : '登录失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

watch([() => props.modelValue, () => props.mode], ([isVisible]) => {
  if (isVisible) resetForm()
})
</script>

<style scoped lang="scss">
:deep(.platform-auth-dialog) {
  max-width: calc(100vw - 32px);
  border-radius: 6px;
  background: var(--sx-surface);

  .el-dialog__header {
    margin-right: 0;
    padding-bottom: 0;
  }

  .el-dialog__body {
    padding-top: 20px;
  }
}
</style>
