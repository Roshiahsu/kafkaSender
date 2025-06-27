package com.example.KafkaSender.handler;

import com.alibaba.fastjson.JSON;
import com.example.KafkaSender.common.BuEnum;
import com.example.KafkaSender.common.KafkaSenderConstants;
import com.example.KafkaSender.common.MPEnum;
import com.example.KafkaSender.model.KafkaDataBaseDTO;
import com.example.KafkaSender.model.KafkaSenderEntity;
import com.example.KafkaSender.model.MessageDTO;
import com.example.KafkaSender.utils.CheckTopicExistence;
import com.example.KafkaSender.utils.SenderUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.PostConstruct;
import java.util.*;

import static com.example.KafkaSender.common.KafkaSenderConstants.*;

@Slf4j
public abstract class AbstractKafkaHandler implements IMQHandler {
    @Autowired
    private KafkaSenderConstants kafkaSenderConstants;
    @Autowired
    @Qualifier("devKafkaTemplate")
    private KafkaTemplate<String, String> devKafkaTemplate;

    @Autowired
    @Qualifier("dockerKafkaTemplate")
    private KafkaTemplate<String, String> dockerKafkaTemplate;

    @Autowired
    @Qualifier("uatKafkaTemplate")
    private KafkaTemplate<String, String> uatKafkaTemplate;

    @Autowired
    @Qualifier("prdKafkaTemplate")
    private KafkaTemplate<String, String> prdKafkaTemplate;

    @Autowired
    private CheckTopicExistence checkTopicExistence;

    @Value("${partitionDataSize:20}")
    private Integer partitionDataSize;

    @PostConstruct
    public void init() {
        validateKafkaTemplates();
        validatePartitionSize();
    }

    private void validateKafkaTemplates() {
        //init template
        Map<String, KafkaTemplate<String, String>> templates = new HashMap<>();
        templates.put(ENV_PRD, prdKafkaTemplate);
        templates.put(ENV_UAT, uatKafkaTemplate);
        templates.put(ENV_DEV, devKafkaTemplate);
        templates.put(ENV_DOCKER, dockerKafkaTemplate);
        //validation
        templates.forEach((env, template) -> {
            if (template == null) {
                log.error("KafkaTemplate for env: {} is not initialized", env);
                throw new IllegalStateException("KafkaTemplate initialization failed for " + env);
            }
        });
    }
    private void validatePartitionSize() {
        if (partitionDataSize == null || partitionDataSize <= 0) {
            log.warn("Invalid partitionDataSize: {}, resetting to default: {}", partitionDataSize, KafkaSenderConstants.DEFAULT_AND_MAX_LIMIT);
            partitionDataSize = KafkaSenderConstants.DEFAULT_AND_MAX_LIMIT;
        } else if (partitionDataSize > KafkaSenderConstants.DEFAULT_AND_MAX_LIMIT) {
            log.warn("partitionDataSize {} exceeds max limit, capping at {}", partitionDataSize, KafkaSenderConstants.DEFAULT_AND_MAX_LIMIT);
            partitionDataSize = KafkaSenderConstants.DEFAULT_AND_MAX_LIMIT;
        }
    }

    @Override
    public void send(KafkaSenderEntity entity) {
        //根據env獲取不同的kafkaTemplate
        KafkaTemplate<String, String> kaTemplate = getTargetKafkaTemplate(entity.getEnv());
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
                result -> log.debug("Sent message to topic: {}, offset: {}, target IP: {}",
                        result.getRecordMetadata().topic(),
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

    private KafkaTemplate<String, String> getTargetKafkaTemplate(String env) {
        switch (env) {
            case ENV_DEV: return devKafkaTemplate;
            case ENV_UAT: return uatKafkaTemplate;
            case ENV_PRD: return prdKafkaTemplate;
            case ENV_DOCKER: return dockerKafkaTemplate;
            default:
                log.error("Unsupported env: {}", env);
                throw new UnsupportedOperationException(SenderUtils.getUnSupportErrMsg(env));
        }
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
        messageDTO.setIpAddress(kafkaSenderConstants.getIpAddress(entity.getEnv()));
        log.debug("MessageDTO : {}", messageDTO);
        return messageDTO;
    }

    protected String initTopic(KafkaSenderEntity entity) {
        return String.format(getTopic(), entity.getMp() , entity.getRegionConfig().getRegionCode());
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
