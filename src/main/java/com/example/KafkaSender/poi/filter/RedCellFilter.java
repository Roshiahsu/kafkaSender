package com.example.KafkaSender.poi.filter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RedCellFilter implements RowFilterCondition {
    @Override
    public boolean shouldSkip(Row row, Workbook workbook) {
        for (Cell cell : row) {
            CellStyle style = cell.getCellStyle();
            if (style != null) {
                short fillColor = style.getFillForegroundColor();
                if (workbook instanceof HSSFWorkbook && fillColor == IndexedColors.RED.getIndex()) {
                    return true;
                } else if (workbook instanceof XSSFWorkbook) {
                    XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
                    XSSFColor color = xssfStyle.getFillForegroundXSSFColor();
                    if (color != null) {
                        byte[] rgb = color.getRGB();
                        if (rgb != null && rgb[0] == (byte)255 && rgb[1] == 0 && rgb[2] == 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
