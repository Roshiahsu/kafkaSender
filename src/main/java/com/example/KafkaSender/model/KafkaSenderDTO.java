package com.example.KafkaSender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class KafkaSenderDTO implements Serializable {
    private String mpName;
    private String buCode;
    private String repullType;
    /**
     * 可以小寫
     * prd , dev , uat
     */
    private String environment;
    private List<String> ids;
    /**
     * update product
     */
    private int size;
    private int startRow;
    private String storeCode;
}
