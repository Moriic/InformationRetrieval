package com.cwc.IR.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 字典树节点结构
class TrieNode {
    Map<Character, TrieNode> children;    // 子节点映射
    boolean isEndOfWord;                  // 标记是否为单词的结束节点

    public TrieNode() {
        children = new HashMap<>();       // 初始化子节点映射
        isEndOfWord = false;
    }
}

public class WildcardQueryUtil {
    private final TrieNode root;  // 字典树根节点
    private final Map<String, String> wordMap = new HashMap<>(); // 存储变体对应的原始单词

    public WildcardQueryUtil() {
        root = new TrieNode();
    }

    // 插入一个单词到字典树中
    private void insert(String word) {
        TrieNode current = root;
        // 遍历每个字符进行插入
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            // 不存在则创建
            current.children.putIfAbsent(ch, new TrieNode());
            current = current.children.get(ch);
        }
        // 标记为单词的结束节点
        current.isEndOfWord = true;
    }

    // 插入字典及其所有变体到字典树中
    public void insertDictionary(String word) {
        // 生成所有变体形式并插入
        for (int i = 0; i <= word.length(); i++) {
            String variant = word.substring(i) + "$" + word.substring(0, i); // 变体形式
            insert(variant);            // 插入变体单词
            wordMap.put(variant, word); // 记录变体对应的原始单词
        }
    }

    // 获取以指定前缀开头的所有单词的数组
    public List<String> getWordsWithPrefix(String prefix) {
        TrieNode current = root;
        // 遍历字典树直到指定前缀的节点
        for (int i = 0; i < prefix.length(); i++) {
            char ch = prefix.charAt(i);
            if (!current.children.containsKey(ch)) {
                return new ArrayList<>();           // 没有以该前缀开头的单词
            }
            current = current.children.get(ch);
        }
        List<String> wordsList = new ArrayList<>();
        getWords(prefix, current, wordsList);       // 获取以指定前缀开头的所有单词
        return wordsList;
    }

    // 辅助方法：递归获取以指定前缀开头的所有单词
    private void getWords(String prefix, TrieNode node, List<String> wordsList) {
        if (node.isEndOfWord) {
            wordsList.add(wordMap.get(prefix));         // 结束添加原始单词到结果列表
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            char ch = entry.getKey();
            TrieNode child = entry.getValue();
            getWords(prefix + ch, child, wordsList); // 递归获取单词
        }
    }

    // 获取与指定单词相等的单词
    public List<String> getWordWithEqual(String word) {
        List<String> wordList = new ArrayList<>();
        TrieNode current = root;
        // 遍历字典树直到与单词相等的节点
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (!current.children.containsKey(ch)) {
                return wordList;                    // 没有该单词
            }
            current = current.children.get(ch);
        }
        if (current != null && current.isEndOfWord) {
            wordList.add(wordMap.get(word));    // 为结束节点则添加原始单词到结果列表
        }
        return wordList;
    }

    public List<String> wildcardQuery(String term) {
        // 遍历单词找到单词中两个'*'的位置, -1表示不存在
        int index1 = -1, index2 = -1;
        for (int i = 0; i < term.length(); i++) {
            if (term.charAt(i) == '*' && index1 == -1)
                index1 = i;
            else if (term.charAt(i) == '*')
                index2 = i;
        }
        // 情况一: *X* -> X*
        if (index1 != -1 && index2 != -1) {
            return getWordsWithPrefix(term.substring(index1 + 1, index2));
        } else {
            // 情况二: X -> X$
            if (index1 == -1) {
                return getWordWithEqual(term + "$");
            } // 情况三: X* -> $X*
            else if (index1 == term.length() - 1) {
                return getWordsWithPrefix("$" + term.substring(0, index1));
            } // 情况四: *X -> X$*
            else if (index1 == 0) {
                return getWordsWithPrefix(term.substring(index1 + 1) + "$");
            } // 情况五: X*Y -> Y$X*
            else {
                return getWordsWithPrefix(term.substring(index1 + 1) + "$"
                        + term.substring(0, index1));
            }
        }
    }
}
