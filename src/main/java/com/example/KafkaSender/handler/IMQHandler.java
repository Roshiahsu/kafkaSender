package com.example.KafkaSender.handler;

import com.example.KafkaSender.model.KafkaSenderEntity;

public interface IMQHandler {
    void send(KafkaSenderEntity entity);

    String getSupportedRepullType();
}
