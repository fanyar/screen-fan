import {createApiClient} from '@/util/request'
import {getVisitorKey} from '@/util/visitor'
import {
  createComment as createMockComment,
  getComments as getMockComments,
  getContentFacets as getMockContentFacets,
  getMockDetail,
  getMockHomeConfig,
  getMockList,
  getModelFacets as getMockModelFacets,
  getModelProgress as getMockModelProgress,
  getModelProgressSnapshots as getMockModelProgressSnapshots,
  getReadingProgress as getMockReadingProgress,
  globalSearch as mockGlobalSearch,
  saveReadingProgress as saveMockReadingProgress,
  submitContactMessage as submitMockContactMessage
} from '@/util/mockApi'

const platformApi = createApiClient({
  baseURL: '/platform-api/api',
  timeout: 8000,
  networkErrorMessage: '后台数据加载失败，请确认服务已启动'
})

const useMock = import.meta.env.VITE_USE_MOCK === 'true'
    || (import.meta.env.PROD && import.meta.env.VITE_USE_MOCK !== 'false')

export const getWorks = params => useMock ? getMockList('works', params) : platformApi.get('/works', {params})
export const getComics = params => useMock ? getMockList('comics', params) : platformApi.get('/comics', {params})
export const getMovies = params => useMock ? getMockList('movies', params) : platformApi.get('/movies', {params})
export const getAnime = params => useMock ? getMockList('anime', params) : platformApi.get('/anime', {params})
export const getArticles = params => useMock ? getMockList('articles', params) : platformApi.get('/articles', {params})
export const getModels = params => useMock ? getMockList('models', params) : platformApi.get('/models', {params})
export const getModelFacets = () => useMock ? getMockModelFacets() : platformApi.get('/models/facets')
export const getModel = id => useMock ? getMockDetail('models', id) : platformApi.get(`/models/${id}`)
export const getModelProgress = id => useMock
    ? getMockModelProgress(id)
    : platformApi.get(`/models/${id}/progress-tree`)
export const getModelProgressSnapshots = id => useMock
    ? getMockModelProgressSnapshots(id)
    : platformApi.get(`/models/${id}/progress/snapshots`)
export const getHomeConfig = () => useMock ? getMockHomeConfig() : platformApi.get('/home-config')
export const getContentFacets = () => useMock ? getMockContentFacets() : platformApi.get('/content-facets')
export const globalSearch = params => useMock ? mockGlobalSearch(params) : platformApi.get('/search', {params})
export const submitContactMessage = data => useMock
    ? submitMockContactMessage(data)
    : platformApi.post('/contact-messages', data)

const visitorHeaders = () => ({headers: {'X-Visitor-Key': getVisitorKey()}})

export const getComments = (contentType, contentId) =>
  useMock ? getMockComments(contentType, contentId) : platformApi.get(`/${contentType}/${contentId}/comments`)
export const createComment = (contentType, contentId, data) =>
  useMock
      ? createMockComment(contentType, contentId, data)
      : platformApi.post(`/${contentType}/${contentId}/comments`, data, visitorHeaders())

export const getReadingProgress = (comicId) =>
  useMock ? getMockReadingProgress(comicId) : platformApi.get(`/comics/${comicId}/progress`, visitorHeaders())
export const saveReadingProgress = (comicId, data) =>
  useMock
      ? saveMockReadingProgress(comicId, data)
      : platformApi.post(`/comics/${comicId}/progress`, data, visitorHeaders())

export const getWork = id => useMock ? getMockDetail('works', id) : platformApi.get(`/works/${id}`, visitorHeaders())
export const getComic = id => useMock ? getMockDetail('comics', id) : platformApi.get(`/comics/${id}`, visitorHeaders())
export const getMovie = id => useMock ? getMockDetail('movies', id) : platformApi.get(`/movies/${id}`, visitorHeaders())
export const getAnimeDetail = id => useMock
    ? getMockDetail('anime', id)
    : platformApi.get(`/anime/${id}`, visitorHeaders())
export const getArticle = id => useMock
    ? getMockDetail('articles', id)
    : platformApi.get(`/articles/${id}`, visitorHeaders())
