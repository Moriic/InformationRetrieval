package com.cwc.IR.utils;

import java.util.Map;

public class CalculateUtil {
    // 计算向量的模
    public static double vectorMagnitude(Map<String, Double> vector) {
        double sum = 0.0;
        for (double component : vector.values()) {
            sum += Math.pow(component, 2);
        }
        return Math.sqrt(sum);
    }

    // 计算两个向量的点积
    public static double dotProduct(Map<String, Double> vector1, Map<String, Double> vector2) {
        double product = 0.0;
        for (String key : vector1.keySet()) {
            if (vector2.containsKey(key)) {
                product += vector1.get(key) * vector2.get(key);
            }
        }
        return product;
    }

    // 计算两个向量的余弦值
    public static double cosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        double dotProduct = dotProduct(vector1, vector2);
        double magnitude1 = vectorMagnitude(vector1);
        double magnitude2 = vectorMagnitude(vector2);
        // 避免除以零
        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0;
        }
        return dotProduct / (magnitude1 * magnitude2);
    }
}
