package com.cwc.IR.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchVO {
    private double time;
    private int total;
    private List<String> tokens;
    private List<DocsVO> docsVOS;
}
