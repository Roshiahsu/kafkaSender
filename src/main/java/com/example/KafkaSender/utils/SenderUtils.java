package com.example.KafkaSender.utils;

import com.example.KafkaSender.common.KafkaSenderConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Objects;

@Slf4j
public class SenderUtils {

    @Autowired
    private CheckTopicExistence checkTopicExistence;

    public static <T> T checkInstance(T object, String msg) {
        if (Objects.isNull(object) || (object instanceof Collection && CollectionUtils.sizeIsEmpty(object))) {
            log.error("{},  Please check the context.", msg);
            throw new RuntimeException(msg);
        }
        return object;
    }

    public static String getUnSupportErrMsg(String param) {
        String msg = String.format("{%s}", param);
        return KafkaSenderConstants.MP_NOT_SUPPORT + msg;
    }

    public static String checkOperation(String operation, String msg) {
        //1.validation
        operation = checkInstance(operation,msg).toUpperCase();
        //2.是否有相關功能提供
        if (KafkaSenderConstants.currentServices.contains(operation) ||
                KafkaSenderConstants.currentENV.contains(operation)) {
            return operation;
        }
        log.error("{},  Please check the context.", msg);
        throw new UnsupportedOperationException(msg);
    }
}
