package com.cwc.IR.utils;

import org.tartarus.snowball.ext.PorterStemmer;

import java.util.ArrayList;
import java.util.List;

/**
 *  对英文句子进行 分词、提取词干
 *  分词 用的是 非字符 进行分词，分词后每个单词开头的大写会变小写
 *  提取词干 用的是 snowball
 */
public class EnTokenizerUtil {
    // 对英文句子进行 分词、提取词干
    public static List<String> tokenize(String text) {
        // 正则表达式提取英文
        String enText = text.replaceAll("[\u4e00-\u9fa5]", "");
        ArrayList<String> tokens = new ArrayList<>();
        // 分词
        List<String> words = enTokenize(enText);
        for (String word : words) {
            // 词干提取，提取后为空""或null就删除
            word = enWordStem(word);
            if (word == null || word.isEmpty() || word.trim().isEmpty()) continue;
            tokens.add(word.trim());
        }
        return tokens;
    }

    /**
     * 英文分词
     * 用 非字符 进行分词，分词后每个单词开头的大写会变小写
     */
    public static List<String> enTokenize(String source) {
        // 创建一个用于存储分词结果的列表
        ArrayList<String> tokens = new ArrayList<>();
        // 创建一个用于累积字符的字符串缓冲区
        StringBuilder buffer = new StringBuilder();

        // 遍历输入字符串的每一个字符
        for (int i = 0; i < source.length(); i++) {
            char character = source.charAt(i);

            // 检查字符是否为字母
            if (Character.isLetter(character)) {
                // 如果是字母，将其追加到缓冲区
                buffer.append(character);
            } else {
                // 如果不是字母，并且缓冲区中有字符
                if (buffer.length() > 0) {
                    // 将缓冲区第一个字符转为小写
                    if (Character.isUpperCase(buffer.charAt(0))) {
                        buffer.setCharAt(0, Character.toLowerCase(buffer.charAt(0)));
                    }
                    // 将缓冲区中的单词添加到结果列表中
                    tokens.add(buffer.toString());
                    // 清空缓冲区
                    buffer = new StringBuilder();
                }
            }
        }

        // 处理最后一个缓冲区中的单词
        if (buffer.length() > 0) {
            if (Character.isUpperCase(buffer.charAt(0))) {
                buffer.setCharAt(0, Character.toLowerCase(buffer.charAt(0)));
            }
            tokens.add(buffer.toString());
        }

        // 返回分词结果列表
        return tokens;
    }


    /**
     * 在英语中，一个单词常常是另一个单词的“变种”，如：happy=>happiness，这里happy叫做happiness的词干（stem）。
     * 在信息检索系统中，我们常常做的一件事，就是在Term规范化过程中，提取词干（stemming），即除去英文单词分词变换形式的结尾
     * 对一个'英语单词'进行词干提取
     * 比如 having 提取后就是have ，binded 提取后就是 bind
     * 注：不是所有的都能正确提取 ，如 had 提取后还是 had ， happy 和 happiness 提取后都是 happi (算法问题，不同算法结果不一样)
     *
     * @param englishWord
     * @return
     */
    public static String enWordStem(String englishWord) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(englishWord);
        if (stemmer.stem()) {
            return stemmer.getCurrent();
        }
        return englishWord;
    }
}

