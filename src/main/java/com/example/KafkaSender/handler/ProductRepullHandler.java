package com.example.KafkaSender.handler;

import com.example.KafkaSender.common.BuEnum;
import com.example.KafkaSender.common.KafkaSenderConstants;
import com.example.KafkaSender.common.MPEnum;
import com.example.KafkaSender.model.KafkaDataBaseDTO;
import com.example.KafkaSender.model.KafkaSenderEntity;
import com.example.KafkaSender.model.data.ProductRepullDTO;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductRepullHandler extends AbstractKafkaHandler {
    @Getter
    private final String msgType = "MP_ORDER_FETCH_PRODUCT_MANUALLY";

    private final String event = "InventoryRepull";
    @Getter
    private final String topic = "OMM_MP_%s_FETCH_PRODUCT_TOPIC_%s";

    @Override
    protected String initRowId(KafkaSenderEntity entity) {
        String buCode = entity.getRegionConfig().getRegionCode();
        String mpName = entity.getMp().name();
        String rowEvent = event + System.currentTimeMillis();
        return String.format("%s-%s-%s-%s", BuEnum.getEnumByBuCode(buCode), entity.getEnv(), mpName, rowEvent);
    }

    @Override
    protected String initBizId(MPEnum mpEnum) {
        return mpEnum.name() + event;
    }

    @Override
    protected KafkaDataBaseDTO prepareKafkaData(KafkaSenderEntity entity, List<String> ids) {
        ProductRepullDTO kafkaDataDTO = new ProductRepullDTO();
        String mp = entity.getMp().name();
        if (entity.getMp().equals(MPEnum.TOKOSHOP)) {
            mp = MPEnum.TOKOPEDIA.name();
        }
        kafkaDataDTO.setMarketplaceEnum(mp);
        kafkaDataDTO.setIds(ids);
        kafkaDataDTO.setProductIdType(entity.getRegionConfig().getProductIdType());
        kafkaDataDTO.setStoreCode(entity.getStoreCode());
        return kafkaDataDTO;
    }

    @Override
    public String getSupportedRepullType() {
        return KafkaSenderConstants.PRODUCT_REPULL;
    }
}
