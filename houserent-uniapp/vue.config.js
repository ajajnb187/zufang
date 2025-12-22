const path = require('path')

module.exports = {
  transpileDependencies: ['@dcloudio/uni-ui'],
  configureWebpack: {
    devtool: process.env.NODE_ENV === 'development' ? 'eval-source-map' : false
  },
  chainWebpack: (config) => {
    config.resolve.alias
      .set('@', path.resolve(__dirname, 'src'))
  }
}
