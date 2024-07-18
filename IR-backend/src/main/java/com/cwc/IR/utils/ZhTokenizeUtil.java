package com.cwc.IR.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 中文分词工具类
 * 使用结巴分词并去除非中文符
 */
public class ZhTokenizeUtil {
    // 创建结巴分词实例
    static JiebaSegmenter segmenter = new JiebaSegmenter();
    // 中文分词
    public static List<String> tokenize(String text) {
        String zhText = extractChineseCharacters(text);
        // 搜索引擎模式
        return segmenter.process(zhText, JiebaSegmenter.SegMode.SEARCH)
                .stream().map(n -> n.word).collect(Collectors.toList());
    }
    // 获取中文字符
    public static String extractChineseCharacters(String text) {
        // 正则表达式匹配中文字符
        Pattern pattern = Pattern.compile("[^a-zA-Z]");
        Matcher matcher = pattern.matcher(text);
        StringBuilder chineseCharacters = new StringBuilder();

        // 迭代匹配结果
        while (matcher.find()) {
            if (matcher.group().trim().isEmpty()) continue;
            chineseCharacters.append(matcher.group());
        }

        return chineseCharacters.toString();
    }
}
