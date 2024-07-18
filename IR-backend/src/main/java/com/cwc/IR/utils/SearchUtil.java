package com.cwc.IR.utils;

import com.cwc.IR.common.ErrorCode;
import com.cwc.IR.exception.ThrowUtils;
import com.cwc.IR.model.DocsVO;
import com.cwc.IR.model.Predata.PreData;
import com.cwc.IR.model.Predata.PreDataFactory;
import com.cwc.IR.model.SimilarityNode;
import com.cwc.IR.model.THUTeacher;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SearchUtil {
    // 计算相似度
    public static List<DocsVO> similaritySearch(String query, List<String> queryTokens, int range) {
        log.info("相似度搜索：{}", query);
        PreData preData = PreDataFactory.getInstance(range);
        // 获取初始数据
        int N = preData.getN();
        List<String> docs = preData.getDocs();
        List<THUTeacher> teachers = preData.getTeachers();
        List<Map<String, Double>> tfIdf = preData.getTf_idf();
        HashMap<String, HashSet<Integer>> df = preData.getDf();

        // 优先队列：计算文档相似度
        PriorityQueue<SimilarityNode> docsSimilarity = new PriorityQueue<>();

        // qyery 分词
        List<String> tokens = MyTokenizerUtil.tokenize(query);
        queryTokens.addAll(tokens);
        // 获取每个 token 的数量
        Map<String, Integer> tokenNum = new HashMap<>();
        for (String token : tokens) {
            tokenNum.put(token, tokenNum.getOrDefault(token, 0) + 1);
        }
        Map<String, Double> tf_idf = new HashMap<>();
        // 计算 tf_idf
        tokenNum.forEach((token, num) -> {
            // idf = log10 N / df
            double cur_idf = df.get(token) == null ? 0 : Math.log10((double) N / df.get(token).size());
            // tf = 1 + log10 tf 或者 0
            double cur_tf = 1 + Math.log10(num);
            // tf-idf = idf * tf
            tf_idf.put(token, cur_idf * cur_tf);
        });

        // 计算相似余弦度
        for (int docId = 0; docId < N; docId++) {
            double cosineSimilarity = CalculateUtil.cosineSimilarity(tfIdf.get(docId), tf_idf);
            if (cosineSimilarity == 0) continue;
            docsSimilarity.add(new SimilarityNode(cosineSimilarity, docId));
        }

        // 将 PriorityQueue 转换为 List<SearchVO>
        return docsSimilarity.stream()
                .map(node -> new DocsVO(
                        node.getId(),
                        docs.get(node.getId()),
                        teachers.get(node.getId()),
                        String.format("%.2f", node.getCosineSimilarity())
                ))
                .collect(Collectors.toList());
    }

    public static List<DocsVO> proximitySearch(String query, List<String> queryTokens, int range) {
        log.info("临近搜索：{}", query);
        PreData preData = PreDataFactory.getInstance(range);
        // 获取 positionalIndex
        Map<String, Map<Integer, List<Byte>>> positionalIndex = preData.getPositionalIndex();
        // 解析 query
        String[] split = query.split(",");
        ThrowUtils.throwIf(split.length != 3, ErrorCode.PARAMS_ERROR, "格式错误([token1,token2,gap])");
        queryTokens.add(split[0]);
        queryTokens.add(split[1]);

        Map<Integer, List<Byte>> map1 = positionalIndex.get(split[0]);
        Map<Integer, List<Byte>> map2 = positionalIndex.get(split[1]);
        if (map1 == null || map2 == null)
            return Collections.emptyList();

        String k = split[2];

        Set<Integer> result = new HashSet<>();
        // 处理 k, 获得左右边界
        int left = 0, right = 0;
        if (k.charAt(0) == '+') {
            right = Integer.parseInt(k.substring(1));
        } else if (k.charAt(0) == '-') {
            left = -1 * Integer.parseInt(k.substring(1));
        } else {
            int rangeK = Integer.parseInt(k);
            left = -rangeK;
            right = rangeK;
        }
        // 遍历两个 map 的指针 p1，p2
        Iterator<Integer> p1 = map1.keySet().iterator();
        Iterator<Integer> p2 = map2.keySet().iterator();
        // 获取两个 map 的第一个 docID
        Integer docID1 = p1.hasNext() ? p1.next() : null;
        Integer docID2 = p2.hasNext() ? p2.next() : null;
        while (docID1 != null && docID2 != null) {
            // 如果两个 docID 相同，则遍历两个 docID 的位置列表，找到满足条件的
            if (docID1.equals(docID2)) {
                List<Integer> l1 = VBEncoderUtil.decodeBytes(map1.get(docID1));   // 获取 docID1 的位置列表
                List<Integer> l2 = VBEncoderUtil.decodeBytes(map2.get(docID2));   // 获取 docID2 的位置列表

                for (int pos1 : l1) {
                    for (int pos2 : l2) {
                        if (pos1 - pos2 >= left && pos1 - pos2 <= right) {
                            result.add(docID1);
                            break;
                        }
                    }
                }

                if (p1.hasNext()) {
                    docID1 = p1.next();
                } else {
                    docID1 = null;
                }
                if (p2.hasNext()) {
                    docID2 = p2.next();
                } else {
                    docID2 = null;
                }
            } else if (docID1 < docID2) {   // 如果 docID1 < docID2，则 docID1 向后移动
                if (p1.hasNext()) {
                    docID1 = p1.next();
                } else {
                    docID1 = null;
                }
            } else {                       // 如果 docID1 > docID2，则 docID2 向后移动
                if (p2.hasNext()) {
                    docID2 = p2.next();
                } else {
                    docID2 = null;
                }
            }
        }
        List<String> docs = preData.getDocs();
        List<THUTeacher> teachers = preData.getTeachers();
        return result.stream()
                .map(docId -> new DocsVO(docId, docs.get(docId), teachers.get(docId), null))
                .collect(Collectors.toList());
    }


    public static List<DocsVO> wildcardSearch(String query, List<String> queryTokens, int range) {
        log.info("通配符搜索：{}", query);
        PreData preData = PreDataFactory.getInstance(range);
        WildcardQueryUtil wildcardQueryUtil = preData.getWildcardQueryUtil();
        List<String> docs = preData.getDocs();
        List<THUTeacher> teachers = preData.getTeachers();
        Map<String, Map<Integer, List<Byte>>> positionalIndex = preData.getPositionalIndex();

        List<String> words = wildcardQueryUtil.wildcardQuery(query);
        List<DocsVO> docsVOS = new ArrayList<>();
        log.info("words：{}", words);
        queryTokens.addAll(words);
        queryTokens.sort(Comparator.comparingInt(String::length).reversed());
        for (String word : words) {
            Map<Integer, List<Byte>> maps = positionalIndex.get(word);
            if (maps == null) continue;
            Set<Integer> docIds = maps.keySet();
            for (Integer docId : docIds) {
                docsVOS.add(new DocsVO(docId, docs.get(docId), teachers.get(docId), null));
            }
        }
        return docsVOS;
    }
}
