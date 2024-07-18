<template>
  <Chat />
  <div class="container">
    <div class="title">面向教师个人主页的信息检索系统</div>
    <!-- 搜索框 -->
    <div class="search-box">
      <input
        class="search-input"
        type="text"
        v-model="query"
        placeholder="输入你想搜索的内容"
        @keyup.enter="search"
      />
      <div class="voice-icon" @click="voiceController">
        <VoiceStartIcon v-show="!isVoice" />
        <div v-show="isVoice" class="identify">
          识别中 00:{{ time < 10 ? `0${time}` : time }}/01:00
        </div>
        <VoiceStopIcon v-show="isVoice" :isVoice="isVoice" />
      </div>
      <div class="search-icon" @click="search">
        <InputIcon />
      </div>
    </div>
    <div class="searchType">
      <div class="radio">
        <span class="label">搜索类型：</span>
        <div class="custom-style">
          <el-segmented v-model="type" :options="types" @change="search" />
        </div>
      </div>
      <div class="radio">
        <span class="label">搜索域：</span>
        <div class="custom-style">
          <el-segmented v-model="range" :options="ranges" @change="search" />
        </div>
      </div>
      <div class="radio">
        <span class="label">query 建议：</span>
        <div class="custom-style">
          <el-segmented
            v-model="query"
            :options="correctedQueries"
            @change="search"
          />
        </div>
      </div>
    </div>

    <div v-if="isShow">
      <div class="time">
        找到 {{ result.total }} 个结果，耗时 {{ result.time }} s
      </div>
      <div>
        <TeacherCard
          :docs="result.docsVOS"
          :tokens="result.tokens"
          :type="type"
          :range="range"
        />
      </div>
    </div>

    <div v-show="result != null && result.total == 0" class="noData">
      找不到和您相符的内容或信息，请重新搜索
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import http from '@/utils/http'
import { XfVoiceDictation } from '@muguilin/xf-voice-dictation'

// 搜索相关
const types = ['相似度搜索', '邻近搜索', '通配符搜索']
const ranges = ['全部', '姓名', '个人简历', '研究领域']
const correctedQueries = ref([])
const query = ref('')
const type = ref('相似度搜索')
const range = ref('全部')
let result: any = ref(null)
const isShow = ref(false)
let originQuery = ref('')
const search = async () => {
  result.value = await http.post('/search', {
    query: query.value,
    type: types.indexOf(type.value),
    range: ranges.indexOf(range.value),
  })
  isShow.value = true
  originQuery.value = query.value
}

watch(originQuery, async () => {
  correctedQueries.value = await http.get(`/correctQuery?query=${query.value}`)
})

// 语音识别相关
const isVoice = ref(false)
const xfVoice = new XfVoiceDictation({
  APPID: '68bec4d3',
  APISecret: 'N2FhNmIxZGRmZjlmYjFhZGZhYTY0NDI2',
  APIKey: 'c10c9ce986fef5d0e048d97b360e94b0',
  // 监听录音状态变化回调
  onWillStatusChange: (oldStatus: number, newStatus: number) => {
    // 可以在这里进行页面中一些交互逻辑处理：注：倒计时（语音听写只有60s）,录音的动画，按钮交互等！
    console.log('识别状态：', oldStatus, newStatus)
  },

  // 监听识别结果的变化回调
  onTextChange: (text: string) => {
    console.log(text)
    query.value = text
  },

  // 监听识别错误回调
  onError: (error: any) => {
    console.log('错误信息：', error)
  },
})

const voiceController = () => {
  isVoice.value = !isVoice.value
  isVoice.value ? xfVoice.start() : xfVoice.stop()
  if (!isVoice.value) search()
  console.log(isVoice.value ? '开始录音' : '结束录音')
}

const time = ref(0)
let intervalId: any = null

watch(isVoice, (newValue) => {
  if (newValue) {
    time.value = 0

    // 清除上一个定时器
    if (intervalId !== null) {
      clearInterval(intervalId)
    }

    intervalId = setInterval(() => {
      if (time.value >= 60) {
        clearInterval(intervalId)
        return
      }
      time.value = time.value + 1
    }, 1000)
  } else {
    // 清除定时器
    if (intervalId !== null) {
      clearInterval(intervalId)
      intervalId = null
    }
  }
})
</script>

<style>
.container {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
  font-family: 微软雅黑, serif;
}

.searchType {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 15px;
  margin: 30px 0 0 20px;

  .radio {
    display: flex;
    align-items: center;
  }
}

.label {
  min-width: 120px;
  font-size: 15px;
  color: #4a4a4a;
  font-weight: bold;
}

.search-box {
  position: relative;
  display: flex;
  width: 100%;
  height: 48px;
  border-radius: 24px;
  padding: 0 24px;
  box-shadow: 2px 2px 2px 4px rgba(0, 0, 0, 0.1);
  background-color: white;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 16px;
  color: #4a4a4a;
}

.search-icon {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  padding: 10px;
}

.voice-icon {
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  padding: 10px;
}

.title {
  font-size: 25px;
  font-weight: bold;
  margin: 50px auto;
  letter-spacing: 3px;
  color: #4664b4;
}

.time {
  color: #6b7280;
  display: flex;
  justify-content: right;
  font-size: 16px;
  line-height: 1.8;
  font-weight: bold;
}

.custom-style .el-segmented {
  --el-segmented-item-selected-color: var(--el-text-color-primary);
  --el-segmented-item-selected-bg-color: #ffd100;
  --el-border-radius-base: 16px;
}

.identify {
  position: absolute;
  top: -30px;
  width: 130px;
  font-size: 14px;
  color: #de5858;
  font-weight: bold;
}

.noData {
  color: #de5858;
  font-size: 18px;
  font-weight: bold;
  margin: 50px auto;
}
</style>
