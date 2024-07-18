<template>
  <div style="display: flex; flex-direction: column; gap: 40px; margin: 30px 0">
    <div class="card" v-for="(item, index) in docs" :key="index">
      <div class="info">
        <div>姓名：{{ item.teacher.title }}</div>
        <div v-if="item.teacher.email != ''">
          邮箱：{{ item.teacher.email }}
        </div>
        <div class="similarity" v-show="type == '相似度搜索'">
          相似度：{{ item.similarity }}
        </div>
        <el-button
          type="primary"
          color="#4664b4"
          @click="goUrl(item.teacher.cnUrl)"
        >
          查看原始页面
        </el-button>
      </div>
      <div class="content">
        <el-tabs v-model="activeName[index]" style="flex: 1">
          <template v-for="(tag, tagIndex) in tags" :key="tagIndex">
            <el-tab-pane :label="tag" :name="tag">
              <el-scrollbar height="270px">
                <div v-html="content[index][tagIndex]"></div>
              </el-scrollbar>
            </el-tab-pane>
          </template>
        </el-tabs>
        <img :src="item.teacher.headerPic" class="img" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

const props = defineProps(['docs', 'tokens', 'type', 'range'])
const goUrl = (url: string) => {
  window.open(url, '_blank')
}
const activeName = ref(Array(props.docs.length).fill('个人简历'))
const tags = ['个人简历', '教学', '研究领域', '研究成果', '奖励荣誉']

const watchDocs = () => {
  activeName.value = activeName.value.map(() =>
    props.range === '全部' || props.range === '姓名' ? '个人简历' : props.range,
  )
  extractContent()
}

const content = ref(Array.from({ length: 10 }, () => Array(5).fill('')))

watch(() => [props.docs], watchDocs)

onMounted(() => {
  watchDocs()
})
const extractContent = () => {
  for (let i = 0; i < props.docs.length; i++) {
    let flag = false
    let doc = props.docs[i].content
    for (let j = 0; j < tags.length; j++) {
      let tag = tags[j]

      // 创建一个正则表达式，用于匹配 <tag> 和 </tag> 之间的内容
      const regex = new RegExp(`<${tag}>(.*?)</${tag}>`, 's')
      const match = doc.match(regex)
      if (match) {
        let extractedContent = match[1]
          .replace(/\n/g, '<br>')
          .replace(/^<br>/, '')
        // 遍历 tokens，并对每个 token 进行替换，添加 <strong> 标签
        props.tokens.forEach((token: string) => {
          const tokenRegex = new RegExp(`(${token})`, 'gi')
          if (
            (props.range === '全部' || props.range === '姓名') &&
            !flag &&
            tokenRegex.test(extractedContent)
          ) {
            activeName.value[i] = tag
            flag = true
          }
          extractedContent = extractedContent.replace(
            tokenRegex,
            '<span style="color: #ff7866; font-weight: bold; margin: 0 2px">$1</span>',
          )
        })
        content.value[i][j] = extractedContent
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.card {
  height: 400px;

  cursor: pointer;
  display: flex;
  align-items: center;
  width: 100%;
  overflow: hidden;
  background-color: white;
  color: #4c4948;
  border-radius: 8px;
  box-shadow: 0 4px 8px 6px rgba(7, 17, 27, 0.06);
  transition: all 0.3s;
  padding: 10px 20px 10px 20px;
  gap: 20px;
  justify-content: space-between;
  flex-direction: column;

  &:hover {
    box-shadow: 0 4px 12px 12px rgba(7, 17, 27, 0.15);
  }
}
.content {
  width: 100%;
  display: flex;
  height: 100%;
  line-height: 1.5;
  gap: 20px;
}
.info {
  display: flex;
  margin-top: 15px;
  gap: 10px;
  color: #4664b4;
  font-weight: bold;
  justify-content: space-between;
  width: 100%;
  align-items: center;
}
.similarity {
  color: #ff7866;
}

.img {
  width: 120px;
  height: auto;
  border-radius: 5px;
  align-self: center;
}

.tags {
  font-size: 14px;
  display: flex;
  justify-content: flex-start;
  gap: 5px;
  color: #797979;
}
</style>
