import {createApiClient} from '@/util/request'
import {getVisitorKey} from '@/util/visitor'

const platformApi = createApiClient({
  baseURL: '/platform-api/api',
  timeout: 8000,
  networkErrorMessage: '后台数据加载失败，请确认服务已启动'
})

export const getWorks = (params) => platformApi.get('/works', {params})
export const getComics = (params) => platformApi.get('/comics', {params})
export const getMovies = (params) => platformApi.get('/movies', {params})
export const getAnime = (params) => platformApi.get('/anime', {params})
export const getArticles = (params) => platformApi.get('/articles', {params})
export const getModels = (params) => platformApi.get('/models', {params})
export const getModelFacets = () => platformApi.get('/models/facets')
export const getModel = (id) => platformApi.get(`/models/${id}`)
export const getModelProgress = (id) => platformApi.get(`/models/${id}/progress-tree`)
export const getModelProgressSnapshots = (id) => platformApi.get(`/models/${id}/progress/snapshots`)
export const getHomeConfig = () => platformApi.get('/home-config')
export const getContentFacets = () => platformApi.get('/content-facets')
export const globalSearch = (params) => platformApi.get('/search', {params})
export const submitContactMessage = (data) => platformApi.post('/contact-messages', data)

const visitorHeaders = () => ({headers: {'X-Visitor-Key': getVisitorKey()}})

export const getComments = (contentType, contentId) =>
  platformApi.get(`/${contentType}/${contentId}/comments`)
export const createComment = (contentType, contentId, data) =>
  platformApi.post(`/${contentType}/${contentId}/comments`, data, visitorHeaders())

export const getReadingProgress = (comicId) =>
  platformApi.get(`/comics/${comicId}/progress`, visitorHeaders())
export const saveReadingProgress = (comicId, data) =>
  platformApi.post(`/comics/${comicId}/progress`, data, visitorHeaders())

export const getWork = (id) => platformApi.get(`/works/${id}`, visitorHeaders())
export const getComic = (id) => platformApi.get(`/comics/${id}`, visitorHeaders())
export const getMovie = (id) => platformApi.get(`/movies/${id}`, visitorHeaders())
export const getAnimeDetail = (id) => platformApi.get(`/anime/${id}`, visitorHeaders())
export const getArticle = (id) => platformApi.get(`/articles/${id}`, visitorHeaders())
