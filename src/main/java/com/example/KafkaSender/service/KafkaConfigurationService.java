package com.example.KafkaSender.service;

import com.example.KafkaSender.common.KafkaSenderConstants;
import com.example.KafkaSender.config.KafkaConfig;
import com.example.KafkaSender.utils.SenderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.example.KafkaSender.common.KafkaSenderConstants.*;

@Service
@Slf4j
public class KafkaConfigurationService {
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

    @Value("${partitionDataSize:20}")
    private Integer partitionDataSize;

    @Autowired
    private KafkaConfig kafkaConfig;

    @PostConstruct
    public void init() {
        validateKafkaTemplates();
        validatePartitionSize();
    }

    /**
     * 驗證template有沒有正確初始化
     */
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

    public KafkaTemplate<String, String> getTargetKafkaTemplate(String env) {
        switch (env) {
            case ENV_DEV:
                return devKafkaTemplate;
            case ENV_UAT:
                return uatKafkaTemplate;
            case ENV_PRD:
                return prdKafkaTemplate;
            case ENV_DOCKER:
                return dockerKafkaTemplate;
            default:
                log.error("Unsupported env: {}", env);
                throw new UnsupportedOperationException(SenderUtils.getUnSupportErrMsg(env));
        }
    }

    public String getIpAddress(String env) {
        return kafkaConfig.getIpAddress(env);
    }
}
