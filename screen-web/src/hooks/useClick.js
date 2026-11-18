import { ref, onUnmounted } from 'vue';

/**
 * 一个 Vue Composition API Hook，用于处理元素的单击、双击和三击事件。
 * 它通过一个时间阈值来区分连续点击。
 *
 * @param {object} [options={}] - 配置选项对象。
 * @param {Function} [options.onSingleClick] - 单击时触发的回调函数。
 * @param {Function} [options.onDbClick] - 双击时触发的回调函数。
 * @param {Function} [options.onTripleClick] - 三击时触发的回调函数。
 * @param {number} [options.threshold=300] - 区分连续点击的时间阈值（毫秒）。
 * 如果在该时间内没有新的点击，则认为点击序列结束。
 * @returns {Function} 一个事件处理函数，应绑定到元素的 `@click` 事件上。
 * 该函数会接收并传递所有原始点击事件的参数。
 */
export function useClick(options = {}) {
  const clickCount = ref(0); // 记录连续点击的次数
  const clickTimer = ref(null); // 用于检测点击间隔的定时器
  const {
    onSingleClick,
    onDbClick,
    onTripleClick,
    threshold: CLICK_THRESHOLD = 300, // 区分连续点击的时间阈值（毫秒）
  } = options;

  function handleClick(...args) {
    clickCount.value++; // 每次点击，点击次数加1

    if (clickTimer.value) {
      // 如果定时器已存在，说明是连续点击，先清除之前的定时器
      clearTimeout(clickTimer.value);
      clickTimer.value = null; // 清除后置为null
    }

    // 设置一个新的定时器
    // 在 CLICK_THRESHOLD 时间后，根据 clickCount 触发相应事件
    clickTimer.value = setTimeout(() => {
      if (clickCount.value === 1) {
        onSingleClick && onSingleClick(...args); // 触发单击回调
      } else if (clickCount.value === 2) {
        onDbClick && onDbClick(...args); // 触发双击回调
      } else if (clickCount.value === 3) {
        onTripleClick && onTripleClick(...args); // 触发三击回调
      }
      // 如果点击次数超过3次，根据需求可以不处理或触发一个默认行为
      // 目前是超出3次就直接不触发任何事件，然后重置计数

      clickCount.value = 0; // 无论触发了什么事件，都重置点击计数
      clickTimer.value = null; // 重置定时器
    }, CLICK_THRESHOLD);
  }

  // 组件卸载时，清理所有未决的定时器
  onUnmounted(() => {
    if (clickTimer.value) {
      clearTimeout(clickTimer.value);
    }
  });

  return handleClick;
}
