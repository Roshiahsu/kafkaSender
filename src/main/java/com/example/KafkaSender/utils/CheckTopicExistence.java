package com.example.KafkaSender.utils;

import com.example.KafkaSender.exception.TopicValidationException;
import com.example.KafkaSender.model.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

@Slf4j
@Component
public class CheckTopicExistence {

    @Value("${kafka.admin.timeout:3000}")
    private int adminTimeout;
    private final Map<String, Long> verifiedTopics = new HashMap<>();
    @Value("${kafka.topic.cache.ttl:300000}")
    private long cacheTtl;
    public void execute(MessageDTO messageDTO) throws Exception {
        String topic = messageDTO.getTopic();
        String ipAddress = messageDTO.getIpAddress();
        String topicCacheKey = topic + ipAddress;
        log.debug("Checking topic existence, bootstrap.servers: {}, topic: {}", ipAddress, topic);

        // 檢查緩存
        Long lastVerifiedTime = verifiedTopics.get(topicCacheKey);
        long currentTime = System.currentTimeMillis();
        if (lastVerifiedTime != null && (currentTime - lastVerifiedTime) < cacheTtl) {
            log.debug("Topic: {} is still valid in cache, skipping verification", topic);
            return; // 緩存有效，直接返回
        }

        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, ipAddress);

        try (AdminClient adminClient = AdminClient.create(props)) {
            DescribeTopicsResult result = adminClient.describeTopics(Collections.singleton(topic),
                    new DescribeTopicsOptions().timeoutMs(adminTimeout));
            Map<String, TopicDescription> topicDescription = result.all().get();
            if (!topicDescription.containsKey(topic)) {
                log.error("Topic does not exist: {}", topic);
                throw new RuntimeException("Topic invalid or does not exist.");
            }
            log.info("Topic validated: {} exists", topic);
            // 驗證成功，存入緩存
            verifiedTopics.put(topicCacheKey, currentTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢復中斷狀態
            log.error("Interrupted while checking topic: {}", topic, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to check topic: {}, bootstrap.servers: {}", topic, ipAddress, e);
            throw e;
        }
    }

    public void executeAsync(MessageDTO messageDTO, Consumer<Map<String, TopicDescription>> onSuccess, Consumer<Throwable> onFailure) {
        if (messageDTO == null || messageDTO.getIpAddress() == null || messageDTO.getTopic() == null) {
            log.error("Invalid MessageDTO: {}", messageDTO);
            onFailure.accept(new IllegalArgumentException("MessageDTO or its properties cannot be null"));
            return;
        }
        String ipAddress = messageDTO.getIpAddress();
        String topic = messageDTO.getTopic();
        log.debug("Async checking topic existence, bootstrap.servers: {}, topic: {}", ipAddress, topic);

        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, ipAddress);

        try (AdminClient adminClient = AdminClient.create(props)) {
            DescribeTopicsResult result = adminClient.describeTopics(Collections.singleton(topic),
                    new DescribeTopicsOptions().timeoutMs(adminTimeout));
            KafkaFuture<Map<String, TopicDescription>> future = result.all();
            future.whenComplete((topicDescription, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to check topic: {}, bootstrap.servers: {}", topic, ipAddress, throwable);
                    onFailure.accept(throwable);
                } else if (!topicDescription.containsKey(topic)) {
                    log.error("Topic does not exist: {}", topic);
                    onFailure.accept(new TopicValidationException("Topic invalid or does not exist: " + topic));
                } else {
                    log.info("Topic validated: {} exists", topic);
                    onSuccess.accept(topicDescription);
                }
            });
        } catch (Exception e) {
            log.error("Failed to initialize AdminClient for topic: {}, bootstrap.servers: {}", topic, ipAddress, e);
            onFailure.accept(e);
        }
    }

    // 可選：清理過期緩存（手動調用或定時任務）
    public void cleanExpiredCache() {
        long currentTime = System.currentTimeMillis();
        verifiedTopics.entrySet().removeIf(entry -> (currentTime - entry.getValue()) >= cacheTtl);
        log.debug("Cleaned expired topics from cache, remaining: {}", verifiedTopics.size());
    }
}
