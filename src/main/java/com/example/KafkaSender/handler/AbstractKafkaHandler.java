package com.example.KafkaSender.handler;

import com.alibaba.fastjson.JSON;
import com.example.KafkaSender.common.BuEnum;
import com.example.KafkaSender.common.KafkaSenderConstants;
import com.example.KafkaSender.common.MPEnum;
import com.example.KafkaSender.model.KafkaDataBaseDTO;
import com.example.KafkaSender.model.KafkaSenderEntity;
import com.example.KafkaSender.model.MessageDTO;
import com.example.KafkaSender.service.KafkaConfigurationService;
import com.example.KafkaSender.utils.CheckTopicExistence;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractKafkaHandler implements IMQHandler {
    @Autowired
    private CheckTopicExistence checkTopicExistence;
    @Autowired
    private KafkaConfigurationService kafkaConfigurationService;
    @Value("${partitionDataSize:20}")
    private Integer partitionDataSize;

    @Override
    public void send(KafkaSenderEntity entity) {
        //根據env獲取不同的kafkaTemplate
        KafkaTemplate<String, String> kaTemplate = kafkaConfigurationService.getTargetKafkaTemplate(entity.getEnv());
        //超過fetch上限，將資料分組
        List<List<String>> dataList = initDataPartition(entity.getIds());
        //初始化kafkaMessage
        dataList.forEach(ids -> {
            try {
                send(initKafkaMessage(entity, ids), kaTemplate);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 發送kafka
     *
     * @param messageDTO
     * @param kaTemplate
     */
    private void send(MessageDTO messageDTO, KafkaTemplate<String, String> kaTemplate) throws Exception {
        //驗證topic
        checkTopicExistence.execute(messageDTO);

        //準備要傳送的data
        //String dataJson = JSON.toJSONString(messageDTO, SerializerFeature.WriteMapNullValue); //SerializerFeature.WriteMapNullValue 要轉換null的variable
        String dataJson;
        try {
            dataJson = JSON.toJSONString(messageDTO);
        } catch (Exception e) {
            log.error("Failed to serialize messageDTO: {}", messageDTO, e);
            return;
        }

        ProducerRecord<String, String> record = new ProducerRecord<String, String>(messageDTO.getTopic(), dataJson);
        Map<String, Object> producerConfigs = kaTemplate.getProducerFactory().getConfigurationProperties();
        String targetIp = (String) producerConfigs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);

        ListenableFuture<SendResult<String, String>> send = kaTemplate.send(record);
        send.addCallback(
                result -> log.debug("Sent message to topic: {}, partition: {}, offset: {}, target IP: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        targetIp),
                ex -> log.error("Failed to send message to topic: {}, message: {}, error: {}",
                        messageDTO.getTopic(),
                        dataJson,
                        ex.getMessage()));

    }

    private void sendAsync(MessageDTO messageDTO, KafkaTemplate<String, String> kaTemplate) {
        checkTopicExistence.executeAsync(
                messageDTO,
                topicDescription -> {
                    String dataJson;
                    try {
                        dataJson = JSON.toJSONString(messageDTO);
                    } catch (Exception e) {
                        log.error("Failed to serialize messageDTO: {}, error: {}", messageDTO, e.getMessage());
                        return;
                    }
                    ProducerRecord<String, String> record = new ProducerRecord<>(messageDTO.getTopic(), dataJson);
                    kaTemplate.send(record).addCallback(
                            result -> log.debug("Sent message to topic: {}, offset: {}", result.getRecordMetadata().topic(), result.getRecordMetadata().offset()),
                            ex -> log.error("Failed to send message to topic: {}, message: {}, error: {}", messageDTO.getTopic(), dataJson, ex.getMessage()));
                },
                throwable -> log.error("Topic validation failed for: {}, error: {}", messageDTO.getTopic(), throwable.getMessage()));
    }

    private List<List<String>> initDataPartition(List<String> data) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }
        return data.size() > KafkaSenderConstants.DEFAULT_AND_MAX_LIMIT ?
                Lists.partition(data, partitionDataSize) : Collections.singletonList(data);
    }


    protected MessageDTO initKafkaMessage(KafkaSenderEntity entity, List<String> data) {
        log.debug("initKafkaMessage start.");
        MessageDTO messageDTO = new MessageDTO();
        String buCode = entity.getRegionConfig().getRegionCode();
        messageDTO.setRowId(initRowId(entity));
        messageDTO.setBuId(Long.parseLong(BuEnum.getEnumByBuCode(buCode).getBuId()));
        messageDTO.setBizId(initBizId(entity.getMp()));
        messageDTO.setData(prepareKafkaData(entity, data));
        messageDTO.setTopic(initTopic(entity));
        messageDTO.setMsgType(getMsgType());
        messageDTO.setIpAddress(kafkaConfigurationService.getIpAddress(entity.getEnv())); //驗證topic是否存在
        log.debug("MessageDTO : {}", messageDTO);
        return messageDTO;
    }

    protected String initTopic(KafkaSenderEntity entity) {
        return String.format(getTopic(), entity.getMp(), entity.getRegionConfig().getRegionCode());
    }

    /*
     abstract method
     */
    protected abstract String getMsgType();

    protected abstract String initRowId(KafkaSenderEntity entity); //buCode env bu

    protected abstract String initBizId(MPEnum entity);

    protected abstract KafkaDataBaseDTO prepareKafkaData(KafkaSenderEntity entity, List<String> ids);

    protected abstract String getTopic();
}
