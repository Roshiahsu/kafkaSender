package com.example.KafkaSender.poi.filter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

public  interface RowFilterCondition {
    boolean shouldSkip(Row row, Workbook workbook);
}
