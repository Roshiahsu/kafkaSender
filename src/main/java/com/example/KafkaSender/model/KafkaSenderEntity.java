package com.example.KafkaSender.model;

import com.example.KafkaSender.common.MPEnum;
import com.example.KafkaSender.utils.SenderUtils;
import lombok.Data;

import java.util.List;

/**
 * mp : marketplace e.g Shopee
 * regionConfig : 不同bu的設定
 * env : 發送環境 e.g prd,uat
 * repullType : product or order
 */
@Data
public class KafkaSenderEntity {
    private MPEnum mp;
    private MPEnum.RegionConfig regionConfig;
    private String env;
    private String repullType;
    private List<String> ids;
    private String storeCode;
    private int size;

    public KafkaSenderEntity(String mpName, String buCode, KafkaSenderDTO dto) {
        this.mp = SenderUtils.checkInstance(MPEnum.fromBuCode(mpName), String.format("Get MPEnum fail. %s is not support", mpName));
        this.regionConfig = SenderUtils.checkInstance(mp.getRegionConfig(buCode), String.format("Get regionConfig fail. %s-%s is not support", mpName, buCode));
        this.repullType = SenderUtils.checkOperation(dto.getRepullType(), String.format("repullType is invalid. repullType : %s", String.valueOf(dto.getRepullType())));
        this.env = SenderUtils.checkOperation(dto.getEnvironment(), String.format("Environment is invalid. Environment : %s", String.valueOf(dto.getEnvironment())));
        this.ids = SenderUtils.checkInstance(dto.getIds(), "Make sure your fetch data.");
        this.size = dto.getSize();
        this.storeCode = dto.getStoreCode();
    }
}
