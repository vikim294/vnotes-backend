package com.vikim.vnotes.admin.audit;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuditLogExcelExporter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static byte[] transferDataToBytes(List<GetAuditLogForExportRow> dataRows){
        //  Workbook 需要通过 try-with-resources 正确关闭
        try(Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("audit logs");

            // row0 表头
            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("日志 ID");
            row0.createCell(1).setCellValue("操作人 ID");
            row0.createCell(2).setCellValue("操作人 email");
            row0.createCell(3).setCellValue("动作");
            row0.createCell(4).setCellValue("描述");
            row0.createCell(5).setCellValue("操作时间");

            // 所有内容行
            int rownum = 1;
            for(GetAuditLogForExportRow dataRow: dataRows){
                Row row = sheet.createRow(rownum);
                // 行的所有单元格
                for(int index = 0; index <= 5; index++){
                    switch (index){
                        case 0 -> {
                            // String.valueOf 和 toString 类似，但是能处理 null -> "null"
                            row.createCell(index).setCellValue(String.valueOf(dataRow.id()));
                        }
                        case 1 -> {
                            row.createCell(index).setCellValue(String.valueOf(dataRow.userId()));
                        }
                        case 2 -> {
                            row.createCell(index).setCellValue(dataRow.email());
                        }
                        case 3 -> {
                            row.createCell(index).setCellValue(dataRow.action().toString());
                        }
                        case 4 -> {
                            row.createCell(index).setCellValue(dataRow.description());
                        }
                        case 5 -> {
                            // LocalDateTime 类是不带时区信息的，需要先转成带时区的 Instant 类，然后转成北京时区
                            row.createCell(index).setCellValue(dataRow.createdAt().toInstant(ZoneOffset.UTC).atZone(ZoneId.of("Asia/Shanghai")).format(FORMATTER));
                        }
                    }
                }
                rownum++;
            }

            // 把 wb 写入到 ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            wb.write(outputStream);

            // 返回 byte[]
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
