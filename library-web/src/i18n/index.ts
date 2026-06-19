import { createI18n } from 'vue-i18n'

import zhCommon from './locales/zh/common.json'
import zhMenu from './locales/zh/menu.json'
import zhError from './locales/zh/error.json'

import enCommon from './locales/en/common.json'
import enMenu from './locales/en/menu.json'
import enError from './locales/en/error.json'

const messages = {
  zh: {
    common: zhCommon,
    menu: zhMenu,
    error: zhError
  },
  en: {
    common: enCommon,
    menu: enMenu,
    error: enError
  }
}

const savedLang = localStorage.getItem('library:lang')
const defaultLang = savedLang || (navigator.language.startsWith('zh') ? 'zh' : 'en')

const i18n = createI18n({
  legacy: false,
  locale: defaultLang,
  fallbackLocale: 'en',
  messages
})

export default i18n
