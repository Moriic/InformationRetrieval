<template>
  <div>
    <div ref="iconRef">
      <el-icon size="45" class="el-input__icon chatIcon">🤖</el-icon>
    </div>
    <div class="talkContent" v-show="isShow" ref="talkContentRef">
      <div class="AI">智谱 AI 知识库问答</div>
      <div class="talkShow" ref="talkShowRef">
        <div
          :class="[
            item.person === 'mechanical' ? 'mechanicalTalk' : 'mineTalk',
          ]"
          v-for="(item, index) in talkList"
          :key="index"
        >
          <span>{{ item.say }}</span>
        </div>
      </div>
      <div class="talkInput">
        <form @submit.prevent="getQuestion" class="userSearch">
          <el-input
            placeholder="请输入内容"
            v-model="contentVal"
            @keyup.enter="getQuestion"
          >
            <template #suffix>
              <svg
                viewBox="64 64 896 896"
                focusable="false"
                data-icon="send"
                width="1em"
                height="1em"
                fill="#409eff"
                aria-hidden="true"
              >
                <path
                  d="M931.4 498.9L94.9 79.5c-3.4-1.7-7.3-2.1-11-1.2a15.99 15.99 0 00-11.7 19.3l86.2 352.2c1.3 5.3 5.2 9.6 10.4 11.3l147.7 50.7-147.6 50.7c-5.2 1.8-9.1 6-10.3 11.3L72.2 926.5c-.9 3.7-.5 7.6 1.2 10.9 3.9 7.9 13.5 11.1 21.5 7.2l836.5-417c3.1-1.5 5.6-4.1 7.2-7.1 3.9-8 .7-17.6-7.2-21.6zM170.8 826.3l50.3-205.6 295.2-101.3c2.3-.8 4.2-2.6 5-5 1.4-4.2-.8-8.7-5-10.2L221.1 403 171 198.2l628 314.9-628.2 313.2z"
                ></path>
              </svg>
            </template>
          </el-input>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

const talkContentRef = ref(null)
const iconRef = ref(null)
const talkShowRef = ref(null)
const isShow = ref(false)
const talkList = ref([
  { id: '1', person: 'mechanical', say: '你好，有什么可以帮到你呢？' },
])
let contentVal = ref<string>('')
let eventSource: any = null
const getQuestion = () => {
  if (contentVal.value === '') {
    return
  }
  // admin提问数据push()
  talkList.value.push({
    id: Date.now(),
    person: 'admin',
    say: contentVal.value,
  })
  setTimeout(() => {
    talkShowRef.value.scrollTop = talkShowRef.value.scrollHeight
  }, 300)
  let question = contentVal.value
  // 清空输入栏数据
  contentVal.value = ''
  // 调用API获取回答， 返回的数据流式输出
  talkList.value.push({ id: Date.now(), person: 'mechanical', say: '' })

  if (eventSource) {
    eventSource.close()
  }
  eventSource = new EventSource(
    `http://localhost:8100/chat?question=${question}`,
  )
  eventSource.onmessage = (event: any) => {
    if (event.data === '回答结束!') eventSource.close()
    const length = talkList.value.length
    talkList.value[length - 1].say += event.data
    talkShowRef.value.scrollTop = talkShowRef.value.scrollHeight
  }
}
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

const handleClickOutside = (event: MouseEvent) => {
  if (iconRef.value && iconRef.value.contains(event.target)) {
    isShow.value = !isShow.value
  } else if (
    isShow.value &&
    talkContentRef.value &&
    !talkContentRef.value.contains(event.target)
  ) {
    isShow.value = false
  }
}
</script>

<style scoped>
.chatIcon {
  position: fixed;
  right: 50px;
  bottom: 50px;
  user-select: none;
}

.AI {
  margin: 10px 0 15px 0;
  text-align: center;
  font-size: 16px;
  color: #4664b4;
  font-weight: bold;
}

.talkContent {
  position: fixed;
  right: 120px;
  bottom: 50px;
  background: #ffffff;
  width: 430px;
  font-size: 14px;
  padding: 15px;
  border-radius: 5px;
  border: 2px solid rgb(214, 216, 219);
  letter-spacing: 1px;
  line-height: 1.2;
  z-index: 100;
}

.talkShow {
  height: 600px;
  overflow: auto;
}

.talkInput {
  width: 100%;
  margin-top: 25px;
}

.mechanicalTalk span {
  display: inline-block;
  background: white;
  border-radius: 10px;
  padding: 5px 10px;
  border: 1px solid rgb(214, 216, 219);
  border-top-left-radius: 0px;
  word-break: break-all;
  text-align: left;
}

.mineTalk {
  margin: 10px;
  text-align: right;
}

.mineTalk span {
  display: inline-block;
  border-radius: 10px;
  border-top-right-radius: 0px;
  background: #409eff;
  color: #fff;
  padding: 5px 10px;
  word-break: break-all;
  text-align: left;
}
</style>
