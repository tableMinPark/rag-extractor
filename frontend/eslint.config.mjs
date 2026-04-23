import nextCoreWebVitals from 'eslint-config-next/core-web-vitals'
import prettierConfig from 'eslint-config-prettier'

const config = [
  ...nextCoreWebVitals,
  prettierConfig,
  {
    ignores: ['public/**/*.js'],
  },
]

export default config
