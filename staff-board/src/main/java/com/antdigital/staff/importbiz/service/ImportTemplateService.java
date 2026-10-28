package com.antdigital.staff.importbiz.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 导入模板下载服务（生成含表头 xlsx）。
 */
@Service
public class ImportTemplateService {

    private static final List<String> HEADERS = java.util.Arrays.asList(
            "工号", "姓名", "部门", "岗位", "入职日期", "邮箱", "手机号", "成本类型", "预算年度", "预算月份", "预算金额");

    public byte[] buildTemplate() {
        List<List<String>> headers = new ArrayList<>();
        for (String header : HEADERS) {
            List<String> column = new ArrayList<>();
            column.add(header);
            headers.add(column);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out)
                .head(headers)
                .sheet("员工导入模板")
                .registerWriteHandler(styleStrategy())
                .doWrite(new ArrayList<>());
        return out.toByteArray();
    }

    private HorizontalCellStyleStrategy styleStrategy() {
        WriteCellStyle headStyle = new WriteCellStyle();
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        WriteCellStyle contentStyle = new WriteCellStyle();
        contentStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        return new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }
}