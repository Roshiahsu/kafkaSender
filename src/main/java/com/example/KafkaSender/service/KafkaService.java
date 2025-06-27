package com.example.KafkaSender.service;

import com.example.KafkaSender.KafkaSenderFactory;
import com.example.KafkaSender.handler.IMQHandler;
import com.example.KafkaSender.model.KafkaSenderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class KafkaService {

    @Autowired
    private KafkaSenderFactory kafkaSenderFactory;

    public void send(KafkaSenderEntity entity) {
        IMQHandler sender = getKafkaHandler(entity.getRepullType());
        Optional.ofNullable(sender).orElseThrow(() -> {
            String msg = "get KafkaHandler fail.";
            log.error(msg);
            return new RuntimeException(msg);
        }).send(entity);
    }

    private IMQHandler getKafkaHandler(String serviceType) {
//        if (!KafkaSenderConstants.currentServices.contains(serviceType)) {
//            throw new UnsupportedOperationException(SenderUtils.getUnSupportErrMsg(serviceType));
//        }
        return kafkaSenderFactory.getKafkaSender(serviceType);
    }
}
