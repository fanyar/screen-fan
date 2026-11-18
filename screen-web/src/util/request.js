import axios from "axios";
import {ElMessage} from "element-plus"

export function createApiClient({baseURL, timeout = 8000, networkErrorMessage}) {
  const client = axios.create({baseURL, timeout})

  client.interceptors.response.use(
      (response) => response.data,
      (error) => {
        ElMessage.error(apiErrorMessage(error, networkErrorMessage))
        return Promise.reject(error)
      }
  )

  return client
}

function apiErrorMessage(error, networkErrorMessage) {
  if (error.response) {
    return error.response.data?.message || `请求失败（HTTP ${error.response.status}）`
  }
  if (error.code === "ECONNABORTED") {
    return "请求超时，请稍后重试"
  }
  return networkErrorMessage || "网络异常，请检查网络连接"
}

const request = createApiClient({baseURL: "/api", timeout: 5000})

export default request

