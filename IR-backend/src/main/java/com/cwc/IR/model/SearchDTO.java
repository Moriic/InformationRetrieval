package com.cwc.IR.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDTO {
    private String query;
    // 0 - 相似度搜索 1 - 邻近搜索 2 - 通配符搜索
    private int type;
    // 0 - 全部 1 - 姓名 2 - 个人简历 3 - 研究领域
    private int range;
}

