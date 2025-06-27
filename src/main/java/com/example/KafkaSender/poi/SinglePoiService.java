package com.example.KafkaSender.poi;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SinglePoiService extends PoiService {
    public <T> List<T> parseExcel(Class<T> tClass, int startRow) {
        if (workbook == null || tClass == null) {
            log.error("Workbook or target class is null, cannot parse Excel");
            throw new IllegalArgumentException("Workbook and target class must not be null");
        }
        List<T> excelDataList = new ArrayList<>();
        //遍歷每一個sheet
        for (Sheet sheet : workbook) {
            String sheetName = sheet.getSheetName();
            log.debug("Parsing sheet: {}", sheetName);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                log.warn("Sheet {} is empty or has no data, skipping", sheetName);
                continue;
            }
            int firstRowNum = sheet.getFirstRowNum();
            int rowEnd = sheet.getPhysicalNumberOfRows();
            Row firstRow = sheet.getRow(firstRowNum);
            if (firstRow == null) {
                log.warn("Sheet {} has no header row, skipping", sheetName);
                continue;
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
            }
        }
        return excelDataList;
    }
}
