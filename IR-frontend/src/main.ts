import { createApp } from 'vue'
import App from './App.vue'
import '@/styles/index.scss'
import 'virtual:svg-icons-register'
import registerGlobComp from '@/components'
import pinia from '@/store'
// import ElementPlus from 'element-plus'
// import 'element-plus/dist/index.css'
import router from './router'

const app = createApp(App)
app.use(registerGlobComp)
// app.use(ElementPlus)
app.use(pinia)
app.use(router)

app.mount('#app')
