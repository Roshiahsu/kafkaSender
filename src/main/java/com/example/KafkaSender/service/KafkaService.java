package com.example.KafkaSender.service;

import com.example.KafkaSender.KafkaSenderFactory;
import com.example.KafkaSender.handler.IMQHandler;
import com.example.KafkaSender.model.KafkaSenderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaService {

    @Autowired
    private KafkaSenderFactory kafkaSenderFactory;

    public void send(KafkaSenderEntity entity) {
        getKafkaHandler(entity.getRepullType()).send(entity);
    }

    private IMQHandler getKafkaHandler(String serviceType) {
        return kafkaSenderFactory.getKafkaSender(serviceType);
    }
}
