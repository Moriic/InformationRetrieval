package com.cwc.IR.utils;

import java.util.ArrayList;
import java.util.List;

public class VBEncoderUtil {

    // VB编码单个数值
    public static List<Byte> encodeNumber(int number) {
        List<Byte> bytes = new ArrayList<>();
        while (true) {
            bytes.add(0, (byte)(number % 128));
            if (number < 128) {
                break;
            }
            number /= 128;
        }
        int lastIndex = bytes.size() - 1;
        bytes.set(lastIndex, (byte)(bytes.get(lastIndex) + 128));
        return bytes;
    }

    // VB解码
    public static List<Integer> decodeBytes(List<Byte> byteList) {
        List<Integer> numbers = new ArrayList<>();
        int previous = -1;
        int number = 0;
        for (byte b : byteList) {
            if (b < 0) { // 如果最高位是1，表示这是最后一个字节
                number = number * 128 + (b & 0x7F);
                numbers.add(previous == -1 ? number : previous + number);
                previous = number;
                number = 0;
            } else {
                number = number * 128 + b;
            }
        }
        return numbers;
    }
}
