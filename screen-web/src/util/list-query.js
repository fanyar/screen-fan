const LIST_QUERY_TEXT_KEYS = new Set([
  'keyword',
  'tag',
  'year',
  'category',
  'status',
  'genre'
])

const MAX_PAGE_NUMBER = 1000000

function firstQueryValue(value) {
  return Array.isArray(value) ? value[0] : value
}

function queryText(value) {
  const source = firstQueryValue(value)
  return source === null || source === undefined ? '' : String(source).trim()
}

function queryInteger(value) {
  const source = Number.parseInt(firstQueryValue(value), 10)
  return Number.isFinite(source) && source > 0 && source <= MAX_PAGE_NUMBER ? source : 0
}

function appendRouteQuery(target, key, value) {
  if (value === '' || value === null || value === undefined) return
  target[key] = value
}

export function readListRouteQuery(source, defaults, allowedPageSizes = []) {
  const query = {...defaults}
  LIST_QUERY_TEXT_KEYS.forEach(key => {
    if (key in defaults) query[key] = queryText(source?.[key])
  })

  const page = queryInteger(source?.page)
  if (page) query.page = page

  const pageSize = queryInteger(source?.pageSize)
  const pageSizeAllowed = !pageSize
      || allowedPageSizes.includes(pageSize)
      || pageSize === defaults.pageSize
  if (pageSizeAllowed && pageSize) query.pageSize = pageSize

  return query
}

export function toListRouteQuery(state, defaults) {
  const query = {}
  LIST_QUERY_TEXT_KEYS.forEach(key => {
    if (key in state) appendRouteQuery(query, key, queryText(state[key]))
  })

  const page = queryInteger(state.page)
  if (page && page !== 1) appendRouteQuery(query, 'page', page)

  const pageSize = queryInteger(state.pageSize)
  if (pageSize && pageSize !== defaults.pageSize) appendRouteQuery(query, 'pageSize', pageSize)

  return query
}

export function preservedListRouteQuery(source) {
  const query = {}
  LIST_QUERY_TEXT_KEYS.forEach(key => {
    appendRouteQuery(query, key, queryText(source?.[key]))
  })

  const page = queryInteger(source?.page)
  if (page) appendRouteQuery(query, 'page', page)

  const pageSize = queryInteger(source?.pageSize)
  if (pageSize) appendRouteQuery(query, 'pageSize', pageSize)

  return query
}
