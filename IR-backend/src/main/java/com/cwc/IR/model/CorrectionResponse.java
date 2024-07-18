package com.cwc.IR.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class CorrectionResponse {
    @SerializedName("char")
    private List<List<Object>> char1;
    private List<List<Object>> word;
    private List<List<Object>> idm;

}
