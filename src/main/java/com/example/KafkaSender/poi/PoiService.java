package com.example.KafkaSender.poi;

import com.example.KafkaSender.annotation.ExcelColumn;
import com.example.KafkaSender.poi.filter.RowFilterCondition;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PoiService {
    @Value("${excel.base.path:C:/Users/HP/Downloads/}")
    private String basePath; // 從配置讀取路徑
    public static final String XLS = ".xls";
    public static final String XLSX = ".xlsx";
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();
    private final Map<String, String[]> headerConfig = new HashMap<>();
    protected final List<RowFilterCondition> filters = new ArrayList<>();

    // 緩存紅色樣式，按 Workbook 類型區分
    private final Map<Workbook, CellStyle> redStyleCache = new WeakHashMap<>();
    private final Map<Sheet, Set<Integer>> headerCache = new WeakHashMap<>();
    @Value("${excel.headers.invoiceNumber:InvoiceNumber}")
    private String invoiceNumberHeaders;

    @Value("${excel.headers.createTime:createTime}")
    private String createTimeHeaders;

    @Value("${excel.headers.status:Status}")
    private String statusHeaders;
    @Getter
    protected Workbook workbook;
    @PostConstruct
    public void initHeaderConfig() {
        headerConfig.put("excel.headers.invoiceNumber", invoiceNumberHeaders.split(","));
        headerConfig.put("excel.headers.createTime", createTimeHeaders.split(","));
        headerConfig.put("excel.headers.status", statusHeaders.split(","));
    }

    // 創建紅色樣式
    private CellStyle createOrGetCachedRedCellStyle(Workbook workbook) {
        return redStyleCache.computeIfAbsent(workbook, wb -> {
            CellStyle style = wb.createCellStyle();
            if (wb instanceof HSSFWorkbook) {
                style.setFillForegroundColor(IndexedColors.RED.getIndex());
            } else if (wb instanceof XSSFWorkbook) {
                XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
                xssfStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)255, 0, 0}, null));
            }
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            return style;
        });
    }

    protected Set<Integer> findTargetHeaderColumnIndices(Sheet sheet, Row firstRow) {
        return headerCache.computeIfAbsent(sheet, wb -> {
            // 收集所有配置中的表頭
            List<String> allTargetHeaders = Arrays.stream(headerConfig.get("excel.headers.invoiceNumber"))
                    .distinct()
                    .collect(Collectors.toList());

            Set<Integer> targetColumnIndexes = new HashSet<>();
            for (Cell cell : firstRow) {
                if (cell != null) {
                    String cellValue = convertCellValueToString(cell);
                    if (cellValue != null && allTargetHeaders.stream().anyMatch(h -> h.equalsIgnoreCase(cellValue))) {
                        targetColumnIndexes.add(cell.getColumnIndex());
                    }
                }
            }
            return targetColumnIndexes;
        });
    }

    // 標記目標列為紅色
    protected void markTargetColumns(Workbook workbook, Row row,Set<Integer> targetColumnIndexes) {
        CellStyle redStyle = createOrGetCachedRedCellStyle(workbook);

        if (targetColumnIndexes.isEmpty()) {
            log.warn("No target headers found in header row for marking in sheet {}", row.getSheet().getSheetName());
            return;
        }

        for (int targetColumnIndex : targetColumnIndexes) {
            Cell targetCell = row.getCell(targetColumnIndex);
            String targetValue = convertCellValueToString(targetCell);
            if (targetValue == null || targetValue.trim().isEmpty()) {
                continue;
            }
            if (targetCell == null) {
                targetCell = row.createCell(targetColumnIndex);
            }
            targetCell.setCellStyle(redStyle);
        }
    }

    /**
     * 回存檔案
     * @param workbook
     * @throws IOException
     */
    public void saveWorkbook(Workbook workbook, String docName) throws IOException {
        String[] split = docName.split("\\.");
        String fullPath = basePath + split[0] + "-process." + split[1]; // 拼接基礎路徑和文件名
        try (FileOutputStream outputStream = new FileOutputStream(fullPath)) {
            workbook.write(outputStream);
            log.info("Workbook saved successfully to: {}", fullPath);
        } catch (IOException e) {
            log.error("Failed to save workbook to {}: {}", fullPath, e.getMessage());
            throw e;
        }
    }

    /**
     * factory
     * @return
     */
    public Workbook getWorkbook(String docName) {
        log.debug("docName : {}", docName);
        if (docName == null || docName.trim().isEmpty()) {
            log.error("Document name is null or empty");
            throw new IllegalArgumentException("Document name cannot be null or empty");
        }
        String extString = docName.substring(docName.lastIndexOf("."));
        String fullPath = basePath + docName; // 拼接基礎路徑和文件名
        try (InputStream is = new FileInputStream(fullPath)) {
            if (XLS.equals(extString)) { //check type
                workbook = new HSSFWorkbook(is);
            } else if (XLSX.equals(extString)) { //check type
                workbook = new XSSFWorkbook(is);
            } else {
                log.error("Unsupported file extension: {}", extString);
                throw new IllegalArgumentException("Unsupported file type: " + extString);
            }
        } catch (FileNotFoundException e) {
            log.error("File not found: {}", fullPath, e);
            throw new RuntimeException("File not found: " + fullPath, e);
        } catch (IOException e) {
            log.error("Error reading file: {}", fullPath, e);
            throw new RuntimeException("Error reading file: " + fullPath, e);
        }
        return workbook;
    }

    protected <T> T createBean(Class<T> tClass) {
        T result = null;
        try {
            Constructor<T> constructor = tClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            result = constructor.newInstance();
        } catch (Exception e) {
            log.error("Failed to create bean for class {}: {}", tClass.getName(), e.getMessage());
        }
        return result;
    }

    public String convertCellValueToString(Cell cell) {
        if (cell == null) {
            return null;
        }
        String returnValue = null;
        switch (cell.getCellType()) {
            case NUMERIC:
                Double doubleValue = cell.getNumericCellValue();
                DecimalFormat df = new DecimalFormat("0");
                returnValue = df.format(doubleValue);
                break;
            case STRING:
                returnValue = cell.getStringCellValue();
                break;
            case BOOLEAN:
                Boolean booleanValue = cell.getBooleanCellValue();
                returnValue = booleanValue.toString();
                break;
            case BLANK:
                break;
            case FORMULA:
                returnValue = cell.getCellFormula();
                break;
            case ERROR:
                break;
            default:
                break;
        }
        return returnValue;
    }

    /**
     * 反射
     *
     * @param row
     * @param firstRow
     * @param bean
     * @param <T>
     */
    protected  <T> void mapRowToBean(Row row, Row firstRow, T bean) {
        if (row == null || firstRow == null || bean == null) {
            log.warn("Skipping mapping due to null row, firstRow, or bean: row={}, firstRow={}, bean={}", row, firstRow, bean);
            return;
        }

        // 緩存表頭名稱到索引的映射，提升效能
        Map<Integer, String> headerMap = buildHeaderMap(firstRow);
        if (headerMap.isEmpty()) {
            log.warn("No valid headers found in first row for sheet {}", firstRow.getSheet().getSheetName());
            return;
        }

        Map<String, Field> fieldMap = getFieldMap(bean.getClass());
        Iterator<Cell> cellIterator = row.cellIterator();

        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            int columnIndex = cell.getColumnIndex();
            String headerValue = headerMap.get(columnIndex);
            if (headerValue == null || headerValue.trim().isEmpty()) {
                continue;
            }

            Field field = fieldMap.get(headerValue);
            if (field != null) {
                String cellValue = convertCellValueToString(cell).trim();
                field.setAccessible(true);
                try {
                    field.set(bean, cellValue);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    log.error("Failed to set field '{}' with value '{}' for bean {} at row {}",
                            headerValue, cellValue, bean.getClass().getSimpleName(), row.getRowNum(), e);
                }
            }
        }
    }

    protected boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            if (row.getCell(i) != null && row.getCell(i).toString().trim().length() > 0) {
                return false;
            }
        }
        return true;
    }

    private Map<Integer, String> buildHeaderMap(Row firstRow) {
        Map<Integer, String> headerMap = new HashMap<>();
        Iterator<Cell> headerCells = firstRow.cellIterator();
        while (headerCells.hasNext()) {
            Cell headerCell = headerCells.next();
            String headerValue = convertCellValueToString(headerCell);
            if (headerValue != null && !headerValue.trim().isEmpty()) {
                headerMap.put(headerCell.getColumnIndex(), headerValue.trim());
            }
        }
        return headerMap;
    }

    private Map<String, Field> getFieldMap(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, k -> {
            Map<String, Field> map = new HashMap<>();
            for (Field field : k.getDeclaredFields()) {
                ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                if (annotation != null) {
                    String[] headers = headerConfig.getOrDefault(annotation.configKey(), new String[]{});
                    for (String header : headers) {
                        map.put(header, field); // 每個表頭名稱都映射到同一字段
                    }
                }
            }
            return map;
        });
    }

    protected boolean skipRowByFilters(Row row) {
        for (RowFilterCondition filter : filters) {
            if (filter.shouldSkip(row, workbook)) {
//                        log.debug("Skipping row {} in sheet {} due to filter condition", rowNum, sheetName);
                return true;
            }
        }
        return false;
    }

    public void addFilters(RowFilterCondition filter) {
        filters.add(filter);
    }
}
