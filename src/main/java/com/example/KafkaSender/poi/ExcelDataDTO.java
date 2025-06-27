package com.example.KafkaSender.poi;


import com.example.KafkaSender.annotation.ExcelColumn;
import lombok.Data;

@Data
public class ExcelDataDTO {
    @ExcelColumn(configKey = "excel.headers.invoiceNumber")
    private String invoiceNumber;
    @ExcelColumn(configKey = "excel.headers.createTime")
    private String createTime;
    @ExcelColumn(configKey = "excel.headers.status")
    private String status;
}
