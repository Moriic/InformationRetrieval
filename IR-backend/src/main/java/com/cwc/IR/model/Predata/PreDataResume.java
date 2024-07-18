package com.cwc.IR.model.Predata;

import com.cwc.IR.model.THUTeacher;
import com.cwc.IR.utils.MyTokenizerUtil;
import com.cwc.IR.utils.VBEncoderUtil;
import com.cwc.IR.utils.WildcardQueryUtil;
import com.huaban.analysis.jieba.JiebaSegmenter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Slf4j
public class PreDataResume implements PreData {
    private static PreDataResume instance = new PreDataResume();

    // 存放所有词及其出现位置的映射表
//    Map<String, Map<Integer, ArrayList<Integer>>> positionalIndex = new TreeMap<>();
    // 存放所有词及其出现位置的映射表（使用VB编码后的间隔）
    Map<String, Map<Integer, List<Byte>>> positionalIndex = new TreeMap<>();
    // 存储文档集
    List<String> docs = new ArrayList<>();
    // 存储文档集中的 token
    List<List<String>> docsTokens = new ArrayList<>();
    // 集合存储 term
    Set<String> terms = new TreeSet<>();
    // 计算 tf：每个 term 在每条文档中的出现次数，key：term，value：<key：docId，value：num>
    HashMap<String, HashMap<Integer, Integer>> tf = new HashMap<>();
    // 计算 df：每个 term 在文档集中的出现次数，key：term，value：docId集合，次数即为集合大小
    HashMap<String, HashSet<Integer>> df = new HashMap<>();
    // 使用 Map 存储 tf_idf：key：term，value：tf_idf
    List<Map<String, Double>> tf_idf = new ArrayList<>();
    // 文档集大小
    int N = 0;
    List<THUTeacher> teachers = new ArrayList<>();

    JiebaSegmenter segmenter = new JiebaSegmenter();

    WildcardQueryUtil wildcardQueryUtil = new WildcardQueryUtil();

    PreDataALL preDataALL = PreDataALL.newInstance();

    private PreDataResume() {
        Init();
        log.info("个人简历数据初始化完毕");
    }

    // 编译正则表达式
    Pattern pattern = Pattern.compile("<个人简历>(.*?)</个人简历>",  Pattern.DOTALL);

    private void Init() {
        List<String> allDocs = preDataALL.getDocs();
        List<THUTeacher> allTeachers = preDataALL.getTeachers();
        for (int i = 0; i < allDocs.size(); i++) {
            String doc = allDocs.get(i);
            Matcher matcher = pattern.matcher(doc);
            if (matcher.find()) {
                String match = matcher.group(1);

                teachers.add(allTeachers.get(i));
                // jia
                docs.add(doc);
                // 标记单词位置
                int pos = 0;
                // 标记前一个位置
                Map<String, Integer> prePosition = new HashMap<>();

                // 生成 positionIndex
                List<String> tokens = segmenter.sentenceProcess(match);
                for (String token : tokens) {
                    token = token.trim();
                    if (token.isEmpty()) continue;
                    pos++;
                    // 获取 token 对应的 positionalIndex，不存在则创建
                    Map<Integer, List<Byte>> postListMap = positionalIndex.getOrDefault(token, new TreeMap<>());
                    // 获取 pos 列表，字节存储
                    List<Byte> posList = postListMap.getOrDefault(N, new ArrayList<>());
                    // 第一个 pos /间隔
                    int gap = posList.isEmpty() ? pos : prePosition.get(token) - pos;
                    // 添加文档位置信息
                    posList.addAll(VBEncoderUtil.encodeNumber(gap));
                    // 更新 前一个位置
                    prePosition.put(token, pos);
                    // 更新位置列表
                    postListMap.put(N, posList);
                    // 更新文档列表
                    positionalIndex.put(token, postListMap);
                    // 构建 Trie 树
                    wildcardQueryUtil.insertDictionary(token);
                }

                // 计算相似度相关
                tokens = MyTokenizerUtil.tokenize(match);
                List<String> docsToken = new ArrayList<>();
                // 遍历每一个token并插入到map中
                for (String token : tokens) {
                    token = token.trim();
                    if(token.isEmpty()) continue;  docsToken.add(token);   // 加入 docsToken
                    terms.add(token);       // 加入 term 集合
                    // 当前 docId 加入当前 token 的集合中，最终集合大小即为 df
                    df.computeIfAbsent(token, k -> new HashSet<>()).add(N);
                    // 将当前 token 对应的 docId 的数量加一
                    HashMap<Integer, Integer> count = tf.computeIfAbsent(token, k -> new HashMap<>());
                    count.put(N, count.getOrDefault(N, 0) + 1);
                }
                // 获取每个文档的 token 数组
                docsTokens.add(docsToken);
                N++;
            }
        }
        // Map 存储，key 为 term，value 为 tf-idf
        for (int docId = 0; docId < N; docId++) {
            List<String> tokens = docsTokens.get(docId);
            Map<String, Double> temp = new HashMap<>();
            // 遍历当前文档的 token
            for (String token : tokens) {
                // idf = log10 N / df
                double cur_idf = Math.log10((double) N / df.get(token).size());
                // tf = 1 + log10 tf 或者 0
                double cur_tf = 1 + Math.log10(tf.get(token).get(docId));
                // tf-idf = idf * tf
                temp.put(token, cur_idf * cur_tf);
            }
            tf_idf.add(temp);
        }
    }

    public static PreDataResume newInstance() {
        return instance;
    }
}


