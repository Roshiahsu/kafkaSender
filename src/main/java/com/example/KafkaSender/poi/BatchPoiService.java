package com.example.KafkaSender.poi;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class BatchPoiService extends PoiService {
    public <T> List<T> parseExcel(Sheet sheet, Class<T> tClass, int startRow, int batchSize) {
        if (workbook == null || tClass == null) {
            log.error("Workbook or target class is null, cannot parse Excel");
            throw new IllegalArgumentException("Workbook and target class must not be null");
        }
        List<T> excelDataList = new ArrayList<>();
        //遍歷每一個sheet
        String sheetName = sheet.getSheetName();
        log.debug("Parsing sheet: {}", sheetName);
        if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
            log.warn("Sheet {} is empty or has no data, skipping", sheetName);
            return null;
        }
        int firstRowNum = sheet.getFirstRowNum();
        int rowEnd = Math.min(sheet.getPhysicalNumberOfRows(), startRow + batchSize);
        Row firstRow = sheet.getRow(firstRowNum);
        if (firstRow == null) {
            log.warn("Sheet {} has no header row, skipping", sheetName);
            return null;
        }

        int rowStart = startRow == 0 ? firstRowNum + 1 : startRow;

        for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null || isRowEmpty(row)) {
                log.debug("Skipping empty row {} in sheet {}", rowNum, sheetName);
                continue;
            }

            // 檢查所有過濾條件
            if (skipRowByFilters(row)) {
                continue;
            }
            T bean = createBean(tClass);
            mapRowToBean(row, firstRow, bean);
            excelDataList.add(bean);

            // 標記目標列為紅色
            markTargetColumns(workbook, row, findTargetHeaderColumnIndices(sheet, firstRow));
        }

        return excelDataList;
    }
}
