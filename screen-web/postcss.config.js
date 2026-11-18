import postcssPxtorem from 'postcss-pxtorem'
import tailwindcss from 'tailwindcss'
import autoprefixer from 'autoprefixer'

export default {
  plugins: [
    tailwindcss(),
    autoprefixer(),
    postcssPxtorem({
      rootValue: 108,
      propList: ['*'],
      selectorBlackList: ['html', 'body', '.el-', '.dv-', '.scrollBar', '.card-container-border'],
      replace: true,
      mediaQuery: false,
      minPixelValue: 1
    })
  ]
}
