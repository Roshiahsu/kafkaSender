package com.example.KafkaSender.handler;

import com.example.KafkaSender.common.BuEnum;
import com.example.KafkaSender.common.KafkaSenderConstants;
import com.example.KafkaSender.common.MPEnum;
import com.example.KafkaSender.model.KafkaDataBaseDTO;
import com.example.KafkaSender.model.KafkaSenderEntity;
import com.example.KafkaSender.model.data.ItemListUpdateDTO;
import com.example.KafkaSender.model.data.OSBMaintenanceDataBaseDTO;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UpdateProductListHandler extends AbstractKafkaHandler {

    @Getter
    private String msgType = "ITEM_LIST_UPDATE";
    @Getter
    private String topic = "OMM_MP_BATCH_UPDATE_TOPIC_%s";

    @Override
    protected String initRowId(KafkaSenderEntity entity) {
        String buCode = entity.getRegionConfig().getRegionCode();
        String mpName = entity.getMp().name();
        return String.format("%s-%s-%s", BuEnum.getEnumByBuCode(buCode), entity.getEnv(), mpName);
    }

    @Override
    protected String initBizId(MPEnum mpEnum) {
        return mpEnum.name();
    }

    @Override
    protected KafkaDataBaseDTO prepareKafkaData(KafkaSenderEntity entity, List<String> ids) {
        int size = entity.getSize();
        String buCode = entity.getRegionConfig().getRegionCode();
        String mpName = entity.getMp().name();
        ItemListUpdateDTO itemListUpdateDTO = new ItemListUpdateDTO();
        itemListUpdateDTO.setChannel(mpName);
        itemListUpdateDTO.setBuCode(buCode);
        ArrayList<OSBMaintenanceDataBaseDTO> updateList = new ArrayList<>();
        itemListUpdateDTO.setUpdateList(updateList);
        for (int i = 0; i < size; i++) {
            OSBMaintenanceDataBaseDTO data = OSBMaintenanceDataBaseDTO.builder()
                    .retekSku("SKU" + i)
                    .grabExternalSkuId("SKU" + i)
                    .isAvailable("TRUE").build();
            updateList.add(data);
        }
        return itemListUpdateDTO;
    }

    @Override
    public String getSupportedRepullType() {
        return KafkaSenderConstants.ITEM_LIST_UPDATE;
    }
}
