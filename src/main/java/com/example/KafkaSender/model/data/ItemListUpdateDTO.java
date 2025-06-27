package com.example.KafkaSender.model.data;

import com.example.KafkaSender.model.KafkaDataBaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ItemListUpdateDTO extends KafkaDataBaseDTO {
    private String buCode;
    private String channel;
    private List<OSBMaintenanceDataBaseDTO> updateList;
}
