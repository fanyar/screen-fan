import {reactive} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {
  readListRouteQuery,
  toListRouteQuery
} from '@/util/list-query'

export function useRouteListQuery(defaults, allowedPageSizes = []) {
  const route = useRoute()
  const router = useRouter()
  const query = reactive(readListRouteQuery(route.query, defaults, allowedPageSizes))

  const routeQuery = () => toListRouteQuery(query, defaults)
  const syncRouteQuery = () => {
    router.replace({path: route.path, query: routeQuery()}).catch(() => {})
  }
  const openDetail = path => router.push({path, query: routeQuery()})

  return {
    query,
    routeQuery,
    syncRouteQuery,
    openDetail
  }
}
