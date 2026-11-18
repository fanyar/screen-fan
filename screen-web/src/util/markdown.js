const HTML_ENTITIES = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#39;'
}

function escapeHtml(value) {
  return value.replace(/[&<>"']/g, character => HTML_ENTITIES[character])
}

function isSafeUrl(value) {
  if (!value || /\s/.test(value)) return false
  return value.startsWith('http://') || value.startsWith('https://') || value.startsWith('/')
}

function formatInline(value) {
  const codeChunks = []
  let result = value.replace(/`([^`]+)`/g, (_match, code) => {
    codeChunks.push(code)
    return `@@CODE_${codeChunks.length - 1}@@`
  })

  result = result.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, (_match, alt, url) => {
    if (!isSafeUrl(url)) return alt
    return `<img src="${url}" alt="${alt}" loading="lazy">`
  })

  result = result.replace(/\[([^\]]+)\]\(([^)]+)\)/g, (_match, text, url) => {
    if (!isSafeUrl(url)) return text
    return `<a href="${url}" target="_blank" rel="noopener noreferrer">${text}</a>`
  })

  result = result
      .replace(/(\*\*|__)([^\n]+?)\1/g, '<strong>$2</strong>')
      .replace(/(?<!\*)\*([^*\n]+)\*(?!\*)/g, '<em>$1</em>')
      .replace(/(?<!_)_([^_\n]+)_(?!_)/g, '<em>$1</em>')

  return result.replace(/@@CODE_(\d+)@@/g, (_match, index) => `<code>${codeChunks[Number(index)]}</code>`)
}

function renderLines(lines) {
  let html = ''
  let index = 0

  while (index < lines.length) {
    const line = lines[index]

    if (!line.trim()) {
      index += 1
      continue
    }

    if (line.startsWith('```')) {
      const language = line.slice(3).replace(/[^a-zA-Z0-9_-]/g, '')
      const codeLines = []
      index += 1
      while (index < lines.length && !lines[index].startsWith('```')) {
        codeLines.push(lines[index])
        index += 1
      }
      index += 1
      html += `<pre><code${language ? ` class="language-${language}"` : ''}>${codeLines.join('\n')}</code></pre>`
      continue
    }

    const heading = line.match(/^(#{1,6})\s+(.+)$/)
    if (heading) {
      const level = heading[1].length
      html += `<h${level}>${formatInline(heading[2])}</h${level}>`
      index += 1
      continue
    }

    if (/^(-{3,}|\*{3,})$/.test(line.trim())) {
      html += '<hr>'
      index += 1
      continue
    }

    if (line.startsWith('&gt;')) {
      const quoteLines = []
      while (index < lines.length && lines[index].startsWith('&gt;')) {
        quoteLines.push(lines[index].replace(/^&gt;\s?/, ''))
        index += 1
      }
      html += `<blockquote>${formatInline(quoteLines.join('\n'))}</blockquote>`
      continue
    }

    const listMatch = line.match(/^\s*[-*]\s+(.+)$/)
    if (listMatch) {
      const items = []
      while (index < lines.length) {
        const item = lines[index].match(/^\s*[-*]\s+(.+)$/)
        if (!item) break
        items.push(`<li>${formatInline(item[1])}</li>`)
        index += 1
      }
      html += `<ul>${items.join('')}</ul>`
      continue
    }

    const orderMatch = line.match(/^\s*\d+[.]\s+(.+)$/)
    if (orderMatch) {
      const items = []
      while (index < lines.length) {
        const item = lines[index].match(/^\s*\d+[.]\s+(.+)$/)
        if (!item) break
        items.push(`<li>${formatInline(item[1])}</li>`)
        index += 1
      }
      html += `<ol>${items.join('')}</ol>`
      continue
    }

    const paragraphLines = []
    while (index < lines.length && lines[index].trim()) {
      const nextHeading = lines[index].match(/^(#{1,6})\s+/)
      const nextBlock = lines[index].startsWith('```') || lines[index].startsWith('&gt;')
      const nextList = /^\s*([-*]|\d+[.])\s+/.test(lines[index])
      if (nextHeading || nextBlock || nextList) break
      paragraphLines.push(formatInline(lines[index]))
      index += 1
    }
    html += `<p>${paragraphLines.join('<br>')}</p>`
  }

  return html
}

export function renderMarkdown(markdown) {
  if (!markdown || !markdown.trim()) return ''
  return renderLines(escapeHtml(markdown).split(/\r?\n/))
}
