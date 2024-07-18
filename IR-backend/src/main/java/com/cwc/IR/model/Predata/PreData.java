package com.cwc.IR.model.Predata;

import com.cwc.IR.model.THUTeacher;
import com.cwc.IR.utils.WildcardQueryUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface PreData {
    int getN();

    List<String> getDocs();

    List<THUTeacher> getTeachers();

    List<Map<String, Double>> getTf_idf();

    HashMap<String, HashSet<Integer>> getDf();

    Map<String, Map<Integer, List<Byte>>> getPositionalIndex();

    WildcardQueryUtil getWildcardQueryUtil();
}
