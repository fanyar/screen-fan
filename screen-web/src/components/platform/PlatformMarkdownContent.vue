<template>
  <div class="markdown-content" v-html="renderedContent"></div>
</template>

<script setup>
import {computed} from 'vue'
import {renderMarkdown} from '@/util/markdown'

const props = defineProps({
  markdown: {
    type: String,
    default: ''
  }
})

const renderedContent = computed(() => renderMarkdown(props.markdown))
</script>

<style scoped lang="scss">
.markdown-content {
  color: var(--sx-body);
  font-size: 16px;
  line-height: 28px;

  :deep(h2) {
    margin: 30px 0 12px;
    color: var(--sx-title);
    font-size: 22px;
    line-height: 31px;
  }

  :deep(h3) {
    margin: 24px 0 10px;
    color: var(--sx-title);
    font-size: 18px;
    line-height: 27px;
  }

  :deep(p) {
    margin: 12px 0;
  }

  :deep(ul),
  :deep(ol) {
    margin: 12px 0;
    padding-left: 24px;
  }

  :deep(li) {
    margin: 6px 0;
  }

  :deep(blockquote) {
    margin: 18px 0;
    padding: 10px 16px;
    border-left: 3px solid var(--sx-primary);
    border-radius: 4px;
    background: var(--sx-primary-soft);
  }

  :deep(code) {
    padding: 2px 6px;
    border-radius: 4px;
    background: var(--sx-canvas);
    color: var(--sx-primary);
    font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
    font-size: 14px;
  }

  :deep(pre) {
    overflow-x: auto;
    margin: 18px 0;
    padding: 16px;
    border-radius: 6px;
    background: #111827;

    code {
      padding: 0;
      background: transparent;
      color: #e5e7eb;
      font-size: 14px;
      line-height: 24px;
    }
  }

  :deep(img) {
    max-width: 100%;
    border-radius: 6px;
  }

  :deep(a) {
    color: var(--sx-primary);

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(hr) {
    margin: 24px 0;
    border-color: var(--sx-line);
  }
}
</style>
