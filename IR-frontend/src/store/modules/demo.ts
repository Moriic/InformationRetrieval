import { defineStore } from 'pinia'
import { ref } from 'vue'

export default defineStore('counter', () => {
  const count = ref<number>(0)

  const increment = () => {
    count.value++
  }

  return {
    count,
    increment,
  }
})
