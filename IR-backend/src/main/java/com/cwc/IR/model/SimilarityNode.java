package com.cwc.IR.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimilarityNode implements Comparable<SimilarityNode> {
    double cosineSimilarity;
    int id;

    @Override
    public int compareTo(SimilarityNode other) {
        // 首先按照cosineSimilarity降序排列
        int result = Double.compare(other.cosineSimilarity, this.cosineSimilarity);
        // 如果cosineSimilarity相同，则按照id升序排列
        return result == 0 ? Integer.compare(this.id, other.id) : result;
    }
}
