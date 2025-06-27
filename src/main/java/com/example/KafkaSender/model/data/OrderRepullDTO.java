package com.example.KafkaSender.model.data;

import com.alibaba.fastjson.annotation.JSONType;
import com.example.KafkaSender.model.KafkaDataBaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JSONType(orders = {"marketplaceEnum", "externalOrderIds", "externalOrderIdType"})
public class OrderRepullDTO extends KafkaDataBaseDTO {
    private List<String> externalOrderIds;
    private String externalOrderIdType;
}
