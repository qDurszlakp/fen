package com.sandbox.server.common.kafka;

import com.sandbox.util.kafka.KafkaTopicInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FooLogConsumer {

    @KafkaListener(topics = "#{T(com.sandbox.util.kafka.KafkaTopicInfo).FOO_LOGGING.getTopicName()}",
                   groupId = "#{T(com.sandbox.util.kafka.KafkaTopicInfo).FOO_LOGGING.getConsumerGroupId()}")
    public void consumeFooLog(@Payload String message,
                              @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        log.info("[Server] Received message from topic {} (partition {}): {}",
                KafkaTopicInfo.FOO_LOGGING.getTopicName(), partition, message);
    }
} 