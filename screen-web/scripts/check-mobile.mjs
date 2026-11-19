import {spawn} from 'node:child_process'
import {mkdir, mkdtemp, rm, writeFile} from 'node:fs/promises'
import {tmpdir} from 'node:os'
import {dirname, join} from 'node:path'
import {fileURLToPath} from 'node:url'
import WebSocket from 'ws'

const scriptDir = dirname(fileURLToPath(import.meta.url))
const outputDir = '/tmp/screen-fan-mobile-check'
const chromeCandidates = [
  process.env.CHROME_PATH,
  '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'
].filter(Boolean)
const chromePath = chromeCandidates[0]

function parseArgs(argv) {
  const options = {
    baseUrl: 'https://fanyar.github.io/screen-fan/',
    listPath: '/platform/articles',
    articleId: '',
    outputDir
  }

  for (let index = 0; index < argv.length; index += 2) {
    const key = argv[index]
    const value = argv[index + 1]
    if (key === '--base-url') options.baseUrl = value
    if (key === '--list-path') options.listPath = value
    if (key === '--article-id') options.articleId = value
    if (key === '--output-dir') options.outputDir = value
  }

  options.baseUrl = options.baseUrl.replace(/\/?$/, '/')
  options.listPath = options.listPath.replace(/^\/?/, '/')
  return options
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

async function fetchJson(url) {
  const response = await fetch(url, {cache: 'no-store'})
  if (!response.ok) throw new Error(`GET ${url} failed: HTTP ${response.status}`)
  return response.json()
}

async function waitForDevTools(port, child) {
  for (let attempt = 0; attempt < 50; attempt += 1) {
    if (child.exitCode !== null) {
      throw new Error(`Chrome exited early with code ${child.exitCode}`)
    }
    try {
      return await fetchJson(`http://127.0.0.1:${port}/json/version`)
    } catch {
      await sleep(200)
    }
  }
  throw new Error('Chrome DevTools endpoint did not start')
}

function createClient(webSocketUrl) {
  const socket = new WebSocket(webSocketUrl)
  let messageId = 0
  const pending = new Map()
  const listeners = new Map()

  socket.addEventListener('message', event => {
    const message = JSON.parse(event.data)
    if (message.id && pending.has(message.id)) {
      const {resolve, reject} = pending.get(message.id)
      pending.delete(message.id)
      if (message.error) reject(new Error(message.error.message))
      else resolve(message.result)
      return
    }
    listeners.get(message.method)?.forEach(callback => callback(message.params))
  })

  const ready = new Promise((resolve, reject) => {
    socket.addEventListener('open', resolve)
    socket.addEventListener('error', () => reject(new Error('Chrome DevTools WebSocket failed')))
  })

  return {
    ready,
    on(method, callback) {
      if (!listeners.has(method)) listeners.set(method, [])
      listeners.get(method).push(callback)
    },
    send(method, params = {}) {
      messageId += 1
      const id = messageId
      return new Promise((resolve, reject) => {
        pending.set(id, {resolve, reject})
        socket.send(JSON.stringify({id, method, params}))
      })
    },
    close() {
      socket.close()
    }
  }
}

async function evaluate(client, expression) {
  const result = await client.send('Runtime.evaluate', {
    expression,
    returnByValue: true,
    awaitPromise: true
  })
  if (result.exceptionDetails) throw new Error(result.exceptionDetails.text)
  return result.result.value
}

async function waitForCondition(client, expression, validator, timeout = 20000) {
  const startedAt = Date.now()
  let lastValue
  let lastError = ''

  while (Date.now() - startedAt < timeout) {
    try {
      lastValue = JSON.parse(await evaluate(client, expression))
      if (validator(lastValue)) return lastValue
    } catch (error) {
      lastError = error.message
    }
    await sleep(300)
  }

  throw new Error(`Mobile check timed out. Last error: ${lastError || JSON.stringify(lastValue)}`)
}

function overflowExpression() {
  return `(() => {
    const viewportWidth = window.innerWidth
    const bad = []
    for (const node of document.querySelectorAll('body *')) {
      const style = getComputedStyle(node)
      if (style.display === 'none' || style.visibility === 'hidden' || node.getAttribute('aria-hidden') === 'true') continue
      const rect = node.getBoundingClientRect()
      if (rect.width < 2 || rect.height < 2) continue
      if (rect.right > viewportWidth + 1 || rect.left < -1 || rect.width > viewportWidth + 1) {
        bad.push({
          tag: node.tagName,
          className: String(node.className).slice(0, 100),
          left: Math.round(rect.left),
          right: Math.round(rect.right),
          width: Math.round(rect.width)
        })
      }
      if (bad.length >= 12) break
    }
    return JSON.stringify({viewportWidth, bad})
  })()`
}

async function captureScreenshot(client, filename) {
  const result = await client.send('Page.captureScreenshot', {
    format: 'png',
    captureBeyondViewport: false
  })
  const target = join(outputDir, filename)
  await mkdir(dirname(target), {recursive: true})
  await writeFile(target, Buffer.from(result.data, 'base64'))
  return target
}

async function main() {
  if (!chromePath) throw new Error('Set CHROME_PATH to a Chrome executable')

  const options = parseArgs(process.argv.slice(2))
  const dataset = await fetchJson(`${options.baseUrl}mock/data.json?ts=${Date.now()}`)
  const articles = dataset.articles?.list?.items || []
  const article = articles.find(item => item.id === options.articleId) || articles[0]
  if (!article) throw new Error('No articles found in mock data')

  const profileDir = await mkdtemp(join(tmpdir(), 'screen-fan-mobile-'))
  const devToolsPort = 9335
  const chrome = spawn(chromePath, [
    '--headless=new',
    '--disable-gpu',
    '--no-first-run',
    '--disable-extensions',
    `--user-data-dir=${profileDir}`,
    `--remote-debugging-port=${devToolsPort}`,
    'about:blank'
  ], {stdio: ['ignore', 'ignore', 'pipe']})

  let client
  const results = {}
  try {
    await waitForDevTools(devToolsPort, chrome)
    const targets = await fetchJson(`http://127.0.0.1:${devToolsPort}/json/list`)
    const page = targets.find(target => target.type === 'page')
    if (!page) throw new Error('Chrome page target not found')

    client = createClient(page.webSocketDebuggerUrl)
    await client.ready
    await client.send('Page.enable')
    await client.send('Runtime.enable')
    await client.send('Emulation.setDeviceMetricsOverride', {
      width: 390,
      height: 844,
      deviceScaleFactor: 2,
      mobile: true
    })

    await client.send('Page.navigate', {url: `${options.baseUrl}#${options.listPath}`})
    const listState = await waitForCondition(
        client,
        `(() => {
          const titles = [...document.querySelectorAll('.platform-card__title')].map(node => node.textContent.trim())
          const loading = Boolean(document.querySelector('.el-loading-mask:not([style*="display: none"])'))
          return JSON.stringify({titles, loading, url: location.href})
        })()`,
        state => !state.loading && state.titles.length > 0
    )
    const listOverflow = JSON.parse(await evaluate(client, overflowExpression()))
    results.list = {
      url: listState.url,
      titleCount: listState.titles.length,
      expectedTitle: article.title,
      hasExpectedTitle: listState.titles.includes(article.title),
      overflow: listOverflow.bad,
      screenshot: await captureScreenshot(client, 'articles-390.png')
    }

    const detailPath = `/platform/articles/${article.id}`
    await evaluate(client, `location.hash = '${detailPath}'`)
    const detailState = await waitForCondition(
        client,
        `(() => {
          const title = document.querySelector('.article-title')?.textContent.trim() || ''
          const contentLength = document.querySelector('.article-body')?.innerText.replace(/\\s/g, '').length || 0
          const loading = Boolean(document.querySelector('.el-loading-mask:not([style*="display: none"])'))
          return JSON.stringify({title, contentLength, loading, url: location.href})
        })()`,
        state => !state.loading && state.title.length > 0 && state.contentLength > 20
    )
    const detailOverflow = JSON.parse(await evaluate(client, overflowExpression()))
    results.detail = {
      url: detailState.url,
      title: detailState.title,
      contentLength: detailState.contentLength,
      expectedTitle: article.title,
      hasExpectedTitle: detailState.title === article.title,
      overflow: detailOverflow.bad,
      screenshot: await captureScreenshot(client, 'article-detail-390.png')
    }

    const failures = []
    if (!results.list.hasExpectedTitle) failures.push('列表页未找到最新文章标题')
    if (results.list.overflow.length) failures.push(`列表页横向溢出: ${JSON.stringify(results.list.overflow)}`)
    if (!results.detail.hasExpectedTitle) failures.push('详情页标题不一致')
    if (results.detail.overflow.length) failures.push(`详情页横向溢出: ${JSON.stringify(results.detail.overflow)}`)
    if (failures.length) throw new Error(failures.join('\n'))

    console.log(JSON.stringify({
      ok: true,
      viewport: '390x844',
      list: results.list,
      detail: results.detail
    }, null, 2))
  } catch (error) {
    console.error(JSON.stringify({
      ok: false,
      error: error.message,
      results
    }, null, 2))
    process.exitCode = 1
  } finally {
    client?.close()
    chrome.kill()
    await rm(profileDir, {recursive: true, force: true}).catch(() => {})
  }
}

await main()
