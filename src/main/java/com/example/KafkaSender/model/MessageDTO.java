package com.example.KafkaSender.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;

import java.util.Date;

@Data
@JSONType(orders={"rowId","buId","sender","bizId","senderCreateTime","status","deliveryMode","routeKey","orderly",
        "msgType","createDate","updateDate","data","topic","onlySendData","retryTimes","maxRetryTimes","errorType","errorMessage","retryIntervalSeconds","cluster"})
public class MessageDTO {
    private String rowId;
    private Long buId;
    private String sender;
    private String bizId;
    private Date senderCreateTime;
    private Integer status;
    private Byte deliveryMode;
    private String routeKey;
    private Byte orderly;
    private String msgType;
    private Date createDate;
    private Date updateDate;
    private KafkaDataBaseDTO data;
    private String topic;
    private Integer onlySendData;
    private Integer retryTimes;
    private Integer maxRetryTimes;
    private String errorType;
    private String errorMessage;
    private String retryIntervalSeconds;
    private String cluster;

    @JSONField(serialize = false)
    private String ipAddress;

    public MessageDTO() {
        this.sender = "REPULL";
        this.senderCreateTime = new Date(System.currentTimeMillis());
        this.status = 0;
        this.deliveryMode = (byte)1;
        this.orderly = (byte)1;
        this.createDate = senderCreateTime;
        this.updateDate = senderCreateTime;
        this.onlySendData = 2;
        this.retryTimes = 0;
        this.maxRetryTimes = 2;
    }
}
