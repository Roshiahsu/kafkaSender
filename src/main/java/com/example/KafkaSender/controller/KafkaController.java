package com.example.KafkaSender.controller;

import com.example.KafkaSender.JsonResult;
import com.example.KafkaSender.model.KafkaSenderDTO;
import com.example.KafkaSender.model.KafkaSenderEntity;
import com.example.KafkaSender.model.data.XmlSenderDTO;
import com.example.KafkaSender.service.ExcelHandleService;
import com.example.KafkaSender.service.KafkaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class KafkaController {

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private ExcelHandleService excelHandleService;

    @PostMapping("sendMessage")
    public JsonResult execute(@RequestBody KafkaSenderDTO data) {
        String mpName = data.getMpName();
        String buCode = data.getBuCode();
        log.debug("接收到的參數，MP: {},BU: {}, data: {}", mpName, buCode, data);
        try {
            kafkaService.send(new KafkaSenderEntity(mpName, buCode, data));
        } catch (Exception e) {
            return JsonResult.fail(e);
        }
        return JsonResult.ok();
    }

    @PostMapping("processXml")
    public JsonResult xmlHandle(@RequestParam String path,
                                @RequestBody XmlSenderDTO data) {
        log.debug("接收到的參數, path: {}, data: {}", path, data);

        try {
            excelHandleService.execute(path, data);
        } catch (Exception e) {
            return JsonResult.fail(e);
        }
        return JsonResult.ok();
    }

    @PostMapping("batchProcessXml")
    public JsonResult batchProcessXml(@RequestParam String path,
                                      @RequestBody XmlSenderDTO data) {
        log.debug("接收到的參數, path: {}, data: {}", path, data);

        try {
            excelHandleService.batchExecute(path, data);
        } catch (Exception e) {
            return JsonResult.fail(e);
        }
        return JsonResult.ok();
    }
}
