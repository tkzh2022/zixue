import { defineStore } from 'pinia'
import { ref } from 'vue'
import i18n from '@/i18n'

export const useAppStore = defineStore('app', () => {
  const language = ref(localStorage.getItem('library:lang') || 'zh')

  function setLanguage(lang: string) {
    language.value = lang
    localStorage.setItem('library:lang', lang)
    i18n.global.locale.value = lang as any
  }

  return {
    language,
    setLanguage
  }
})
