package com.cwc.IR.model.Predata;

public class PreDataFactory {

    public static PreData getInstance(int range) {
        switch (range) {
            case 0:
                return PreDataALL.newInstance();
            case 1:
                return PreDataName.newInstance();
            case 2:
                return PreDataResume.newInstance();
            case 3:
                return PreDataField.newInstance();
            default:
                return null;
        }
    }
}