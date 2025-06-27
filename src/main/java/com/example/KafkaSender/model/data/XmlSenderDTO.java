package com.example.KafkaSender.model.data;

import com.example.KafkaSender.model.KafkaSenderDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class XmlSenderDTO extends KafkaSenderDTO {
    /**
     * 每次拉幾隻
     */
    private int limit;
    /**
     * 間隔
     */
    private int interval;
}
