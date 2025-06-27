package com.example.KafkaSender.common;

import com.example.KafkaSender.utils.SenderUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum BuEnum {
    WTCID("102", "WTCID"),
    WTCMY("103", "WTCMY"),
    WTCPH("104", "WTCPH"),
    WTCSG("105", "WTCSG"),
    WTCTH("106", "WTCTH"),
    WTCTW("107", "WTCTW"),
    PNSHK("111", "PNSHK"),
    KVN("201", "KVN"),
    KVB("202", "KVB"),
    MIT("203", "MIT"),
    ;
    private final String buId;
    private final String buCode;

    private static final Map<String, BuEnum> CODE_TO_ENUM = Arrays.stream(values())
            .collect(Collectors.toMap(BuEnum::getBuCode, Function.identity()));

    public static BuEnum getEnumByBuCode(String buCode) {
        BuEnum result = CODE_TO_ENUM.get(buCode);
        if (result == null) {
            throw new IllegalArgumentException(SenderUtils.getUnSupportErrMsg(buCode));
        }
        return result;
    }
}
