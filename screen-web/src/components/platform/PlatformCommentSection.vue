<template>
  <section v-if="commentsVisible" class="platform-comment-section">
    <div class="comment-header">
      <h2 class="comment-title">评论 <span v-if="total" class="comment-count">{{ total }}</span></h2>
    </div>

    <div v-loading="loading" class="comment-list">
      <div v-for="item in comments" :key="item.id" class="comment-item">
        <div class="comment-item__avatar">{{ avatarText(item.authorName) }}</div>
        <div class="comment-item__body">
          <div class="comment-item__meta">
            <span class="comment-item__name">{{ item.authorName }}</span>
            <span class="comment-item__time">{{ formatTime(item.createdAt) }}</span>
          </div>
          <p class="comment-item__text">{{ item.body }}</p>
        </div>
      </div>
      <el-empty v-if="!loading && !comments.length" description="还没有评论，来说点什么吧" :image-size="60"/>
    </div>

    <el-form class="comment-form" @submit.prevent="handleSubmit">
      <el-form-item>
        <el-input
            v-model="form.authorName"
            placeholder="昵称"
            maxlength="50"
            show-word-limit
            class="comment-form__name"
        />
      </el-form-item>
      <el-form-item>
        <el-input
            v-model="form.body"
            type="textarea"
            :rows="3"
            placeholder="写下你的想法..."
            maxlength="1000"
            show-word-limit
        />
      </el-form-item>
      <el-form-item>
        <el-button
            type="primary"
            :loading="submitting"
            :disabled="!canSubmit"
            native-type="submit"
        >
          发表评论
        </el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {createComment, getComments} from '@/api/platform'
import {PLATFORM_SHOW_COMMENTS} from '@/const/platform'
import {getVisitorKey} from '@/util/visitor'

const props = defineProps({
  contentType: {type: String, required: true},
  contentId: {type: String, required: true}
})

const commentsVisible = PLATFORM_SHOW_COMMENTS
const comments = ref([])
const loading = ref(false)
const submitting = ref(false)
const form = reactive({authorName: '', body: ''})

const total = computed(() => comments.value.length)
const canSubmit = computed(() => {
  return form.authorName.trim().length > 0 && form.body.trim().length > 0 && !submitting.value
})

const avatarText = (name) => {
  return name ? name.trim().charAt(0).toUpperCase() : '?'
}

const formatTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

const loadComments = async () => {
  if (!props.contentId) return
  loading.value = true
  try {
    comments.value = await getComments(props.contentType, props.contentId)
  } catch {
    comments.value = []
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!canSubmit.value || submitting.value) return
  submitting.value = true
  try {
    await createComment(props.contentType, props.contentId, {
      authorName: form.authorName.trim(),
      body: form.body.trim()
    })
    form.body = ''
    ElMessage.success('评论已发布')
    await loadComments()
  } catch (error) {
    const message = error?.response?.data?.message || '评论发布失败，请稍后重试'
    ElMessage.error(message)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (commentsVisible) loadComments()
})
</script>

<style scoped lang="scss">
.platform-comment-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--sx-line);
}

.comment-header {
  margin-bottom: 16px;
}

.comment-title {
  margin: 0;
  color: var(--sx-title);
  font-size: 20px;
  line-height: 28px;
}

.comment-count {
  margin-left: 6px;
  color: var(--sx-muted);
  font-size: 14px;
  font-weight: 400;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--sx-line);

  &:last-of-type {
    border-bottom: 0;
  }
}

.comment-item__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--sx-primary-soft, rgba(37, 99, 235, 0.1));
  color: var(--sx-primary);
  font-size: 14px;
  font-weight: 500;
}

.comment-item__meta {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 4px;
}

.comment-item__name {
  color: var(--sx-title);
  font-size: 14px;
  font-weight: 500;
}

.comment-item__time {
  color: var(--sx-muted);
  font-size: 12px;
}

.comment-item__text {
  margin: 0;
  color: var(--sx-body);
  font-size: 14px;
  line-height: 22px;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

.comment-form {
  margin-top: 20px;

  .comment-form__name {
    max-width: 240px;
  }
}
</style>
