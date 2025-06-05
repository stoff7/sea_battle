import { createI18n } from 'vue-i18n';
import messages from '@/locales/index.js';

const savedLang = localStorage.getItem('lang') || 'ru'; // РУССКИЙ ЯЗЫК ПО УМОЛЧАНИЮ

export default createI18n({
  legacy: false,
  locale: savedLang,
  fallbackLocale: 'en',
  messages,
});