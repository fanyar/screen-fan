<template>
  <el-form
      ref="formRef"
      class="contact-form"
      :model="formData"
      :rules="formRules"
      label-position="top"
      @submit.prevent="handleSubmit"
  >
    <div class="contact-form__fields">
      <el-form-item label="称呼" prop="name">
        <el-input v-model.trim="formData.name" maxlength="50" placeholder="怎么称呼你"/>
      </el-form-item>
      <el-form-item label="联系方式" prop="contact">
        <el-input v-model.trim="formData.contact" maxlength="100" placeholder="邮箱或手机号"/>
      </el-form-item>
    </div>
    <el-form-item label="留言内容" prop="content">
      <el-input
          v-model.trim="formData.content"
          type="textarea"
          maxlength="1000"
          show-word-limit
          :rows="4"
          placeholder="想聊什么"
      />
    </el-form-item>
    <el-button class="contact-form__submit" type="primary" native-type="submit" :loading="submitting">
      提交留言
    </el-button>
  </el-form>
</template>

<script setup>
import {nextTick, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {submitContactMessage} from '@/api/platform'

const formRef = ref()
const submitting = ref(false)
const formData = reactive({
  name: '',
  contact: '',
  content: ''
})

const formRules = {
  name: [{required: true, message: '请输入称呼', trigger: 'blur'}],
  contact: [{required: true, message: '请输入联系方式', trigger: 'blur'}],
  content: [{required: true, message: '请输入留言内容', trigger: 'blur'}]
}

const resetForm = async () => {
  formData.name = ''
  formData.contact = ''
  formData.content = ''
  await nextTick()
  formRef.value?.clearValidate()
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
    await submitContactMessage({...formData})
    ElMessage.success('留言已提交')
    await resetForm()
  } catch (error) {
    ElMessage.error('留言提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.contact-form {
  max-width: 720px;
  margin: 30px auto 0;
  padding: 24px;
  border: 1px solid var(--sx-line);
  border-radius: 6px;
  background: var(--sx-surface);
  text-align: left;
}

.contact-form__fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.contact-form__submit {
  width: 132px;
}

@media (max-width: 640px) {
  .contact-form__fields {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>
