const setMetaTag = (attribute, key, content) => {
  if (!content) return
  let tag = document.head.querySelector(`meta[${attribute}="${key}"]`)
  if (!tag) {
    tag = document.createElement('meta')
    tag.setAttribute(attribute, key)
    document.head.appendChild(tag)
  }
  tag.setAttribute('content', content)
}

export const setPageSeo = ({title, description, image}) => {
  if (title) {
    document.title = title
  }
  setMetaTag('name', 'description', description)
  setMetaTag('property', 'og:title', title)
  setMetaTag('property', 'og:description', description)
  setMetaTag('property', 'og:image', image)
  setMetaTag('property', 'og:type', 'article')
}
