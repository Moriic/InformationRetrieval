package com.cwc.IR.controller;


import com.cwc.IR.common.BaseResponse;
import com.cwc.IR.common.ResultUtils;
import com.cwc.IR.model.DocsVO;
import com.cwc.IR.model.SearchDTO;
import com.cwc.IR.model.SearchVO;
import com.cwc.IR.utils.SearchUtil;
import com.cwc.IR.utils.TextCorrectionUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SearchController {


    @PostMapping("/search")
    public BaseResponse<SearchVO> search(@RequestBody SearchDTO searchDTO) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        int type = searchDTO.getType();
        String query = searchDTO.getQuery();
        int range = searchDTO.getRange();
        List<DocsVO> docsVO;
        List<String> queryTokens = new ArrayList<>();


        // 记录开始时间
        long startTime = System.nanoTime();
        if (type == 0) {
            docsVO = SearchUtil.similaritySearch(query, queryTokens, range);
        } else if (type == 1) {
            docsVO = SearchUtil.proximitySearch(query, queryTokens, range);
        } else {
            docsVO = SearchUtil.wildcardSearch(query, queryTokens, range);
        }
        int total = docsVO.size();

        docsVO = docsVO.subList(0, Math.min(docsVO.size(), 10));
        queryTokens.remove("*");
        // 记录结束时间
        long endTime = System.nanoTime();
        // 计算耗时
        long durationInNano = endTime - startTime;
        // 将耗时转换为秒，保留两位小数
        double duration = Math.round(durationInNano / 1_000_000_000.0 * 100.0) / 100.0;

        SearchVO searchVO = new SearchVO(duration, total, queryTokens, docsVO);
        return ResultUtils.success(searchVO);
    }

    @GetMapping("/correctQuery")
    public BaseResponse<List<String>> correctQuery(String query) throws Exception {
        List<String> correctedQueries = TextCorrectionUtil.correctQuery(query);
        return ResultUtils.success(correctedQueries);
    }
}
