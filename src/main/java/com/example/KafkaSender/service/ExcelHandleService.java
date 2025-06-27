package com.example.KafkaSender.service;

import com.example.KafkaSender.model.KafkaSenderEntity;
import com.example.KafkaSender.model.data.XmlSenderDTO;
import com.example.KafkaSender.poi.ExcelDataDTO;
import com.example.KafkaSender.poi.SinglePoiService;
import com.example.KafkaSender.poi.BatchPoiService;
import com.example.KafkaSender.poi.filter.RedCellFilter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExcelHandleService {
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private SinglePoiService singlePoiService;
    @Autowired
    private BatchPoiService batchPoiService;

    /**
     * 一次性解析excel
     * @param path
     * @param data
     */
    public void execute(String path, XmlSenderDTO data) {
        String mpName = data.getMpName();
        String buCode = data.getBuCode();
        // 讀取並解析 Excel
        singlePoiService.getWorkbook(path);
        singlePoiService.addFilters(new RedCellFilter());

        List<ExcelDataDTO> excelDataDTOS = singlePoiService.parseExcel(ExcelDataDTO.class, data.getStartRow());

        List<String> invoiceNumbers = excelDataDTOS.stream().map(ExcelDataDTO::getInvoiceNumber)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (invoiceNumbers.isEmpty()) {
            log.warn("No valid invoice numbers found in Excel file at path: {}", path);
            return;
        }
        //TODO 消除魔法值，優化
        int interval = data.getInterval() < 1000 ? 3000 : data.getInterval();

        List<List<String>> batches = Lists.partition(invoiceNumbers, data.getLimit());
        for (List<String> batch : batches) {
            data.setIds(batch);

            try {
                kafkaService.send(new KafkaSenderEntity(mpName, buCode, data));
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted during batch delay", e);
                throw new RuntimeException("Processing interrupted");
            } catch (Exception e) {
                log.error("Failed to send batch to Kafka, mpName: {}, buCode: {}, batch: {}", mpName, buCode, batch, e);
                throw e;
            }
        }
    }

    public void batchExecute(String path, XmlSenderDTO data) {
        String mpName = data.getMpName();
        String buCode = data.getBuCode();
        Workbook workbook = batchPoiService.getWorkbook(path);
        batchPoiService.addFilters(new RedCellFilter());
        int index = 0;
        int startRow = data.getStartRow();
        while (index < workbook.getNumberOfSheets()) {
            Sheet sheet = workbook.getSheetAt(index);
            List<ExcelDataDTO> excelDataDTOS = batchPoiService.parseExcel(sheet ,ExcelDataDTO.class, startRow, data.getLimit());

            List<String> invoiceNumbers = excelDataDTOS.stream().map(ExcelDataDTO::getInvoiceNumber)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (invoiceNumbers.isEmpty() && startRow >= sheet.getPhysicalNumberOfRows()) {
                log.warn("No valid invoice numbers found in Excel file at path: {}", path);
                continue;
            }
            //TODO 消除魔法值，優化
            int interval = data.getInterval() < 1000 ? 3000 : data.getInterval();

            List<List<String>> batches = Lists.partition(invoiceNumbers, data.getLimit());
            for (List<String> batch : batches) {
                data.setIds(batch);

                try {
                    kafkaService.send(new KafkaSenderEntity(mpName, buCode, data));
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted during batch delay", e);
                    throw new RuntimeException("Processing interrupted");
                } catch (Exception e) {
                    log.error("Failed to send batch to Kafka, mpName: {}, buCode: {}, batch: {}", mpName, buCode, batch, e);
                    throw e;
                }

                // 保存結果
                try {
                    batchPoiService.saveWorkbook(workbook, path);
                } catch (IOException e) {
                    log.error("Error processing Excel file: {}", e.getMessage());
                    throw new RuntimeException(e.getMessage());
                }
            }
            startRow += data.getLimit();
            if (startRow > sheet.getPhysicalNumberOfRows()) {
                index++;
                startRow = 0;
            }
        }
    }
}
