package com.cwc.IR.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocsVO {
    private int docId;
    private String content;
    private THUTeacher teacher;
    private String similarity;
}
