package com.example.KafkaSender.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.example.KafkaSender.common.KafkaSenderConstants.*;


@Configuration
@EnableKafka
public class KafkaConfig {
    private final Map<String, String> IP_ADDRESS_MAP = new HashMap<>();
    /*
    IP Address
     */
    @Value("${app.ip-address.prd}")
    private String prdIpAddress;
    @Value("${app.ip-address.uat}")
    private String uatIpAddress;
    @Value("${app.ip-address.dev}")
    private String devIpAddress;
    @Value("${app.ip-address.docker}")
    private String dockerIpAddress;

    public String getIpAddress(String env) {
        return IP_ADDRESS_MAP.getOrDefault(env, "localhost:9092"); // 默認值可以根據需要調整
    }

    @PostConstruct
    public void init() {
        IP_ADDRESS_MAP.put(ENV_PRD, prdIpAddress);
        IP_ADDRESS_MAP.put(ENV_UAT, uatIpAddress);
        IP_ADDRESS_MAP.put(ENV_DEV, devIpAddress);
        IP_ADDRESS_MAP.put(ENV_DOCKER, dockerIpAddress);
    }

    @Bean(name = "devKafkaTemplate")
    public KafkaTemplate<String, String> devKafkaTemplate() {
        return new KafkaTemplate<>(devProducerFactory(ENV_DEV));
    }

    @Bean(name = "dockerKafkaTemplate")
    public KafkaTemplate<String, String> dockerKafkaTemplate() {
        return new KafkaTemplate<>(devProducerFactory(ENV_DOCKER));
    }

    @Bean(name = "uatKafkaTemplate")
    public KafkaTemplate<String, String> uatKafkaTemplate() {
        return new KafkaTemplate<>(devProducerFactory(ENV_UAT));
    }

    @Bean(name = "prdKafkaTemplate")
    public KafkaTemplate<String, String> prdKafkaTemplate() {
        return new KafkaTemplate<>(devProducerFactory(ENV_PRD));
    }

    private ProducerFactory<String, String> devProducerFactory(String env) {
        return new DefaultKafkaProducerFactory<>(devConfigs(env));
    }

    private Map<String, Object> devConfigs(String env) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getIpAddress(env));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }
}
