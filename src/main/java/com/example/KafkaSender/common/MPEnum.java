package com.example.KafkaSender.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
public enum MPEnum {
    TOKOPEDIA(buildRegionMap(RegionConfig.TOKOPEDIA_REGIONS)),
    SHOPEE(buildRegionMap(RegionConfig.SHOPEE_REGIONS)),
    LAZADA(buildRegionMap(RegionConfig.LAZADA_REGIONS)),
    TIKTOK(buildRegionMap(RegionConfig.TIKTOK_REGIONS)),
    TOKOSHOP(buildRegionMap(RegionConfig.TOKO_SHOP_REGIONS)),
    ;

    private final Map<String, RegionConfig> regionConfigMap;

    MPEnum(Map<String, RegionConfig> regionConfigMap) {
        this.regionConfigMap = regionConfigMap;
    }

    public static MPEnum fromBuCode(String buCode) {
        try {
            return valueOf(buCode.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid BU Code: {}", buCode, e);
            return null;
        }
    }

    private static Map<String, RegionConfig> buildRegionMap(RegionConfig[] regions) {
        Map<String, RegionConfig> map = new HashMap<>();
        for (RegionConfig region : regions) {
            map.put(region.getRegionCode(), region);
        }
        return Collections.unmodifiableMap(map);
    }

    public RegionConfig getRegionConfig(String regionCode) {
        return regionConfigMap.get(regionCode);
    }

    @AllArgsConstructor
    public static class RegionConfig {
        static final RegionConfig[] TOKOPEDIA_REGIONS = buildRegions(
                new String[]{"WTCID"},
                "invoice_number",
                "PRODUCT_ID"
        );
        static final RegionConfig[] SHOPEE_REGIONS = buildRegions(
                new String[]{"WTCMY", "WTCPH", "WTCTH", "WTCID", "WTCSG"},
                "order_id",
                "PRODUCT_ID"
        );
        static final RegionConfig[] LAZADA_REGIONS = buildRegions(
                new String[]{"WTCMY", "WTCPH", "WTCTH", "WTCID", "WTCSG"},
                "order_id",
                "OMM_SKU_ID"
        );
        static final RegionConfig[] TIKTOK_REGIONS = buildRegions(
                new String[]{"WTCMY", "WTCPH", "WTCTH", "WTCID"},
                "order_id",
                "PRODUCT_ID"
        );
        static final RegionConfig[] TOKO_SHOP_REGIONS = buildRegions(
                new String[]{"WTCID"},
                "order_id",
                "PRODUCT_ID"
        );
        @Getter
        private final String regionCode;
        @Getter
        private final String externalOrderType;
        @Getter
        private final String productIdType;

        private static RegionConfig[] buildRegions(String[] regionCodes, String externalOrderType, String productIdType) {
            return Arrays.stream(regionCodes)
                    .map(code -> new RegionConfig(code, externalOrderType, productIdType))
                    .toArray(RegionConfig[]::new);
        }
    }
}
