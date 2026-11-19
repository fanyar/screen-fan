import {spawn} from 'node:child_process'
import {readFile} from 'node:fs/promises'
import {dirname, join} from 'node:path'
import {fileURLToPath} from 'node:url'

const scriptDir = dirname(fileURLToPath(import.meta.url))
const webDir = join(scriptDir, '..')
const adminDir = join(webDir, '..', 'screen-admin')
const rootDir = join(webDir, '..')
const pagesUrl = 'https://fanyar.github.io/screen-fan/'
const actionUrl = 'https://github.com/fanyar/screen-fan/actions'

function parseArgs(argv) {
  const options = {
    command: 'help',
    sync: false,
    juejin: false,
    backendTests: false,
    skipLocalMobile: false,
    skipOnlineMobile: false,
    message: ''
  }

  const positional = []
  for (let index = 0; index < argv.length; index += 1) {
    const value = argv[index]
    if (value === '--sync') options.sync = true
    if (value === '--juejin') options.juejin = true
    if (value === '--backend-tests') options.backendTests = true
    if (value === '--skip-local-mobile') options.skipLocalMobile = true
    if (value === '--skip-online-mobile') options.skipOnlineMobile = true
    if (value === '--message') {
      index += 1
      options.message = argv[index] || ''
    }
    if (!value.startsWith('--')) positional.push(value)
  }

  options.command = positional[0] || 'help'
  return options
}

function run(command, args, options = {}) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, {
      cwd: options.cwd || rootDir,
      env: {...process.env, ...options.env},
      stdio: 'inherit'
    })
    child.on('error', reject)
    child.on('exit', code => {
      if (code === 0) resolve()
      else reject(new Error(`${command} ${args.join(' ')} failed with code ${code}`))
    })
  })
}

function startProcess(command, args, options = {}) {
  const child = spawn(command, args, {
    cwd: options.cwd || rootDir,
    env: {...process.env, ...options.env},
    stdio: options.silent ? ['ignore', 'pipe', 'pipe'] : 'inherit'
  })
  if (options.silent) {
    child.stdout.on('data', () => {})
    child.stderr.on('data', () => {})
  }
  return child
}

async function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

async function urlExists(url, validate = response => response.ok) {
  try {
    const response = await fetch(url, {cache: 'no-store'})
    return validate(response)
  } catch {
    return false
  }
}

async function waitForBackend() {
  const healthUrl = 'http://127.0.0.1:8666/api/articles?page=1&pageSize=1'
  for (let attempt = 0; attempt < 5; attempt += 1) {
    if (await urlExists(healthUrl)) return
    await sleep(500)
  }

  console.log('本地后台未运行，正在启动 screen-admin...')
  const server = startProcess('./start-server.sh', [], {
    cwd: adminDir,
    silent: false
  })
  let started = false
  for (let attempt = 0; attempt < 60; attempt += 1) {
    if (server.exitCode !== null) throw new Error('screen-admin 启动失败')
    started = await urlExists(healthUrl)
    if (started) break
    await sleep(1000)
  }
  if (!started) throw new Error('等待 screen-admin 启动超时')
  await sleep(1000)
  return server
}

async function syncBackend({juejin = false}) {
  let server
  try {
    if (juejin) {
      console.log('导入掘金文章元数据和正文...')
      await run('node', ['scripts/import-juejin-articles.mjs'], {cwd: webDir})
    }

    server = await waitForBackend()
    console.log('同步后端数据到静态 mock...')
    await run('npm', ['run', 'sync:mock'], {cwd: webDir})
  } finally {
    server?.kill()
  }
}

async function buildProduction() {
  console.log('构建 GitHub Pages 生产包...')
  await run('npm', ['run', 'build'], {
    cwd: webDir,
    env: {
      VITE_BASE_PATH: '/screen-fan/',
      VITE_USE_MOCK: 'true'
    }
  })
}

async function runBackendTests() {
  console.log('运行后端测试...')
  await run('../.tools/apache-maven-3.9.9/bin/mvn', ['test'], {
    cwd: join(adminDir, 'server')
  })
}

async function startPreview() {
  const port = 4173
  const url = `http://127.0.0.1:${port}/screen-fan/`
  const child = startProcess('npm', [
    'run',
    'preview',
    '--',
    '--host',
    '127.0.0.1',
    '--port',
    String(port),
    '--strictPort'
  ], {
    cwd: webDir,
    env: {
      VITE_BASE_PATH: '/screen-fan/',
      VITE_USE_MOCK: 'true'
    },
    silent: true
  })

  for (let attempt = 0; attempt < 50; attempt += 1) {
    if (child.exitCode !== null) throw new Error('Vite preview 启动失败')
    if (await urlExists(url)) return {child, url}
    await sleep(200)
  }
  child.kill()
  throw new Error('等待 Vite preview 启动超时')
}

async function checkLocalMobile() {
  console.log('构建已完成，开始移动端 390x844 检查...')
  const preview = await startPreview()
  try {
    await run('node', ['scripts/check-mobile.mjs', '--base-url', preview.url], {cwd: webDir})
  } finally {
    preview.child.kill()
  }
}

async function gitCommitAndPush(message) {
  const status = await new Promise((resolve, reject) => {
    const child = spawn('git', ['status', '--short'], {cwd: rootDir})
    let output = ''
    child.stdout.on('data', data => {
      output += data
    })
    child.on('error', reject)
    child.on('exit', code => code === 0 ? resolve(output.trim()) : reject(new Error('git status failed')))
  })

  if (!status) {
    console.log('没有新的文件变更；继续部署当前 HEAD。')
    return
  }

  console.log('待提交变更：')
  console.log(status)
  await run('git', ['add', '-A'], {cwd: rootDir})
  await run('git', ['commit', '-m', message], {cwd: rootDir})
}

async function pushAndWatchActions() {
  const headSha = await new Promise((resolve, reject) => {
    const child = spawn('git', ['rev-parse', 'HEAD'], {cwd: rootDir})
    let output = ''
    child.stdout.on('data', data => {
      output += data
    })
    child.on('error', reject)
    child.on('exit', code => code === 0 ? resolve(output.trim()) : reject(new Error('git rev-parse failed')))
  })

  await run('git', ['push', 'origin', 'main'], {cwd: rootDir})
  console.log(`已推送 ${headSha}，等待 Deploy screen-web 工作流...`)
  const startedAt = Date.now()

  while (Date.now() - startedAt < 10 * 60 * 1000) {
    await sleep(10000)
    const response = await fetch(
        `https://api.github.com/repos/fanyar/screen-fan/actions/runs?head_sha=${headSha}&per_page=1`,
        {headers: {'User-Agent': 'screen-fan-deploy-script'}}
    )
    if (!response.ok) throw new Error(`GitHub API failed: HTTP ${response.status}`)
    const payload = await response.json()
    const run = payload.workflow_runs?.[0]
    if (!run) {
      console.log('工作流尚未启动...')
      continue
    }

    console.log(`部署状态: ${run.status}${run.conclusion ? ` / ${run.conclusion}` : ''}`)
    if (run.status === 'completed') {
      if (run.conclusion !== 'success') {
        throw new Error(`GitHub Actions 失败: ${run.html_url}`)
      }
      return run
    }
  }

  throw new Error(`等待 GitHub Actions 超时: ${actionUrl}`)
}

async function readLocalManifest() {
  const file = join(webDir, 'public/mock/manifest.json')
  return JSON.parse(await readFile(file, 'utf8'))
}

async function verifyPages() {
  const localManifest = await readLocalManifest()
  console.log('验证线上 Pages 数据...')

  for (let attempt = 0; attempt < 15; attempt += 1) {
    try {
      const response = await fetch(`${pagesUrl}mock/manifest.json?ts=${Date.now()}`, {cache: 'no-store'})
      if (response.ok) {
        const remoteManifest = await response.json()
        if (remoteManifest.generatedAt === localManifest.generatedAt) {
          console.log(`线上数据已更新：${remoteManifest.counts?.articles ?? 0} 篇文章`)
          break
        }
        console.log(`线上数据尚未更新：remote=${remoteManifest.generatedAt}, local=${localManifest.generatedAt}`)
      }
    } catch (error) {
      console.log(`Pages 请求失败：${error.message}`)
    }
    await sleep(10000)
  }

  const remoteManifest = await fetch(`${pagesUrl}mock/manifest.json?ts=${Date.now()}`, {cache: 'no-store'})
  if (!remoteManifest.ok) throw new Error(`线上 manifest 请求失败: HTTP ${remoteManifest.status}`)
  const remoteData = await remoteManifest.json()
  if (remoteData.generatedAt !== localManifest.generatedAt) {
    throw new Error('线上 mock 数据仍未更新到本次构建版本')
  }
}

async function verifyOnlineMobile() {
  console.log('验证线上移动端 390x844 展示...')
  await run('node', ['scripts/check-mobile.mjs', '--base-url', pagesUrl], {cwd: webDir})
}

function printHelp() {
  console.log(`
用法:
  node screen-web/scripts/deploy-sync.mjs sync [--juejin]
  node screen-web/scripts/deploy-sync.mjs check
  node screen-web/scripts/deploy-sync.mjs deploy [--sync] [--juejin] [--message "commit message"]

示例:
  node screen-web/scripts/deploy-sync.mjs sync
  node screen-web/scripts/deploy-sync.mjs deploy --sync --message "Sync articles and update styles"

默认流程包含:
  1. 数据同步（--sync 时）
  2. GitHub Pages 生产构建
  3. 本地 390x844 移动端检查
  4. 提交并推送 main
  5. 等待 GitHub Actions 成功
  6. 线上数据和移动端展示验证
`)
}

async function main() {
  const options = parseArgs(process.argv.slice(2))

  if (options.command === 'help') {
    printHelp()
    return
  }

  if (options.command === 'sync') {
    await syncBackend({juejin: options.juejin})
    console.log('数据同步完成。')
    return
  }

  if (options.command !== 'check' && options.command !== 'deploy') {
    throw new Error(`未知命令: ${options.command}。使用 help 查看用法。`)
  }

  if (options.sync) await syncBackend({juejin: options.juejin})
  if (options.backendTests) await runBackendTests()
  await buildProduction()
  if (!options.skipLocalMobile) await checkLocalMobile()

  if (options.command === 'check') {
    console.log('本地检查完成，未提交或部署。')
    return
  }

  if (!options.message) {
    throw new Error('部署必须提供 --message "提交说明"')
  }
  await gitCommitAndPush(options.message)
  await pushAndWatchActions()
  await verifyPages()
  if (!options.skipOnlineMobile) await verifyOnlineMobile()
  console.log(`部署完成：${pagesUrl}#/platform/articles`)
}

try {
  await main()
} catch (error) {
  console.error(`执行失败：${error.message}`)
  process.exitCode = 1
}
