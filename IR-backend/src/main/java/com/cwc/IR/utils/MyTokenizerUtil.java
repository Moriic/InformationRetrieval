package com.cwc.IR.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 分词工具类
 * 中文使用结巴分词
 * 英文使用 Porter algorithm
 */
public class MyTokenizerUtil {

    public static List<String> tokenize(String text) {
        // 中文分词和英文分词
        List<String> zhTokens = ZhTokenizeUtil.tokenize(text);
        List<String> enTokens = EnTokenizerUtil.tokenize(text);
        // 合并结果
        zhTokens.addAll(enTokens);
        return zhTokens;
    }
}
