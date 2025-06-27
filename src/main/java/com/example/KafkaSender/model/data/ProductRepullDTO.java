package com.example.KafkaSender.model.data;

import com.alibaba.fastjson.annotation.JSONType;
import com.example.KafkaSender.model.KafkaDataBaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JSONType(orders = {"marketplaceEnum", "productIdType", "ids"})
public class ProductRepullDTO extends KafkaDataBaseDTO {
    private String productIdType;
    private List<String> ids;
}

