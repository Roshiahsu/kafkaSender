package com.example.KafkaSender.config;

import com.example.KafkaSender.common.KafkaSenderConstants;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableKafka
public class KafkaConfig {

    @Autowired
    private KafkaSenderConstants kafkaSenderConstants;

    @Bean(name = "devKafkaTemplate")
    public KafkaTemplate<String, String> devKafkaTemplate() {
        return new KafkaTemplate<>(devProducerFactory(KafkaSenderConstants.ENV_DEV));
    }

    @Bean(name = "dockerKafkaTemplate")
    public KafkaTemplate<String, String> dockerKafkaTemplate() { return new KafkaTemplate<>(devProducerFactory(KafkaSenderConstants.ENV_DOCKER)); }

    @Bean(name = "uatKafkaTemplate")
    public KafkaTemplate<String, String> uatKafkaTemplate() {
        return new KafkaTemplate<>(devProducerFactory(KafkaSenderConstants.ENV_UAT));
    }

    @Bean(name = "prdKafkaTemplate")
    public KafkaTemplate<String, String> prdKafkaTemplate() {
        return new KafkaTemplate<>(devProducerFactory(KafkaSenderConstants.ENV_PRD));
    }

    private ProducerFactory<String, String> devProducerFactory(String env) {
        return new DefaultKafkaProducerFactory<>(devConfigs(env));
    }

    private Map<String, Object> devConfigs(String env) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaSenderConstants.getIpAddress(env));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }
}
