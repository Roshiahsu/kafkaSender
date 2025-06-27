package com.example.KafkaSender.model.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OSBMaintenanceDataBaseDTO {
    private String retekStoreId; // storeCode
    private String retekSku;
    private String isAvailable;

    private String grabmartStoreId; //merchantId
    private String grabExternalSkuId;
}
