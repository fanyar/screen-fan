import {mkdir, readFile, rm, writeFile} from 'node:fs/promises'
import {dirname, join} from 'node:path'
import {fileURLToPath} from 'node:url'
import * as cheerio from 'cheerio'
import TurndownService from 'turndown'

const scriptDir = dirname(fileURLToPath(import.meta.url))
const metadataFile = join(
    scriptDir,
    '../../screen-admin/server/src/main/resources/data/juejin-articles.json'
)
const assetRoot = join(scriptDir, '../public/platform/articles/juejin')
const requestHeaders = {
  'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36'
}
const force = process.argv.includes('--force')

function isPlaceholder(content) {
  return !content || content.includes('站内暂未导入完整正文')
}

function extensionFromUrl(url) {
  const pathname = new URL(url).pathname
  const match = pathname.match(/\.(png|jpe?g|gif|webp|svg)$/i)
  return match ? `.${match[1].toLowerCase()}` : '.webp'
}

async function downloadImage(url, targetFile) {
  const response = await fetch(url, {headers: requestHeaders})
  if (!response.ok) throw new Error(`GET image failed: HTTP ${response.status}`)
  await writeFile(targetFile, Buffer.from(await response.arrayBuffer()))
}

function createTurndownService() {
  const service = new TurndownService({
    headingStyle: 'atx',
    codeBlockStyle: 'fenced',
    bulletListMarker: '-'
  })

  service.addRule('codeBlock', {
    filter: node => node.nodeName === 'PRE' && node.querySelector('code'),
    replacement: (_content, node) => {
      const code = node.querySelector('code')
      const className = code.getAttribute('class') || ''
      const languageMatch = className.match(/(?:^|\s)(?:language-|hljs\s+)([\w#+-]+)/)
      const language = code.getAttribute('lang') || languageMatch?.[1] || ''
      const source = code.textContent.replace(/^\n/, '').replace(/\n$/, '')
      return `\n\n\`\`\`${language}\n${source}\n\`\`\`\n\n`
    }
  })

  return service
}

async function importArticle(item) {
  if (!force && !isPlaceholder(item.content)) return 'skipped'

  let $ = null
  let body = null

  for (let attempt = 1; attempt <= 3; attempt += 1) {
    const response = await fetch(`https://juejin.cn/post/${item.id}`, {headers: requestHeaders})
    if (!response.ok) throw new Error(`GET ${item.id} failed: HTTP ${response.status}`)
    $ = cheerio.load(await response.text())
    body = $('#article-root .article-viewer').first()
    if (body.length) break
    if (attempt === 3) throw new Error(`Article body not found: ${item.id}`)
    await new Promise(resolve => setTimeout(resolve, attempt * 2000))
  }

  body.find('script, style, link').remove()

  const articleAssetDir = join(assetRoot, String(item.id))
  await rm(articleAssetDir, {recursive: true, force: true})
  await mkdir(articleAssetDir, {recursive: true})

  const images = body.find('img').toArray()
  for (const [index, image] of images.entries()) {
    const source = $(image).attr('src')
    if (!source) continue
    const imageUrl = new URL(source, 'https://juejin.cn').href
    const filename = `image-${index + 1}${extensionFromUrl(imageUrl)}`
    await downloadImage(imageUrl, join(articleAssetDir, filename))
    $(image).attr('src', `/platform/articles/juejin/${item.id}/${filename}`)
    $(image).removeAttr('srcset')
  }

  const markdown = createTurndownService().turndown(body.html())
      .replace(/\n{3,}/g, '\n\n')
      .trim()
  item.content = `${markdown}\n\n---\n\n原文：[${item.title}](https://juejin.cn/post/${item.id})`
  await writeFile(metadataFile, `${JSON.stringify(items, null, 2)}\n`, 'utf8')
  return 'imported'
}

const items = JSON.parse(await readFile(metadataFile, 'utf8'))
const imported = []

try {
  for (const item of items) {
    const result = await importArticle(item)
    imported.push({id: item.id, result})
    console.log(`${result}: ${item.title}`)
    await new Promise(resolve => setTimeout(resolve, 800))
  }

  await writeFile(metadataFile, `${JSON.stringify(items, null, 2)}\n`, 'utf8')
  console.log(`Imported ${imported.filter(item => item.result === 'imported').length} of ${items.length} articles`)
} catch (error) {
  console.error(error)
  process.exitCode = 1
}
