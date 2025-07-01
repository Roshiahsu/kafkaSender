package com.example.KafkaSender.common;


import java.util.Arrays;
import java.util.List;

public class KafkaSenderConstants {

    //service type
    public static final String ORDER_REPULL = "ORDER";
    public static final String PRODUCT_REPULL = "PRODUCT";
    public static final String ITEM_LIST_UPDATE = "ITEM_LIST_UPDATE";
    public static final List<String> currentServices = Arrays.asList(ORDER_REPULL, PRODUCT_REPULL, ITEM_LIST_UPDATE);

    //Env type
    public static final String ENV_PRD = "PRD";
    public static final String ENV_UAT = "UAT";
    public static final String ENV_DEV = "DEV";
    public static final String ENV_DOCKER = "DOCKER";
    public static final List<String> currentENV = Arrays.asList(ENV_PRD, ENV_UAT, ENV_DEV, ENV_DOCKER);

    //Message
    public static final String MP_NOT_SUPPORT = "This specific is not support.";

    public static final int DEFAULT_AND_MAX_LIMIT = 20;
    /**
     * localhost:9092 = docker 內的kafka 提供給"外"部使用的ip address
     * kafka:9093 = docker 內的kafka 提供給"內"部使用的ip address
     * docker-compose.yml 中設定
     * KAFKA_CFG_ADVERTISED_LISTENERS=CLIENT://kafka:9093,EXTERNAL://localhost:9092
     */
    public static final String DEV_IP_ADDRESS = "localhost:9092";
    public static final String DOCKER_IP_ADDRESS = "kafka:9093";

}
