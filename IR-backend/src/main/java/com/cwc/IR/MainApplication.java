package com.cwc.IR;

import com.cwc.IR.model.Predata.PreDataALL;
import com.cwc.IR.model.Predata.PreDataField;
import com.cwc.IR.model.Predata.PreDataName;
import com.cwc.IR.model.Predata.PreDataResume;
import com.cwc.IR.utils.TextCorrectionUtil;
import org.openjdk.jol.info.GraphLayout;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MainApplication.class, args);
        PreDataALL preDataALL = PreDataALL.newInstance();
        PreDataName preDataName = PreDataName.newInstance();
        PreDataResume preDataResume = PreDataResume.newInstance();
        PreDataField preDataField = PreDataField.newInstance();
        TextCorrectionUtil.correctQuery("test");
        // 获取两个对象的内存占用
        double size1 = GraphLayout.parseInstance(preDataALL.getPositionalIndexOld()).totalSize() / (1024.0 * 1024.0);
        double size2 = GraphLayout.parseInstance(preDataALL.getPositionalIndex()).totalSize() / (1024.0 * 1024.0);

        // 打印内存占用
        System.out.printf("PositionalIndex Size:  %.2f MB\n", size1);
        System.out.printf("PositionalIndex Size With VB: %.2f MB\n", size2);

        // 计算并打印内存占用差距
        System.out.printf("Difference in size: %.2f MB\n", size1 - size2);
    }
}