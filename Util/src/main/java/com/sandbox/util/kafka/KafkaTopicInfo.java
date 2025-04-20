package com.sandbox.util.kafka;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaTopicInfo {

    FOO_LOGGING("FOO_LOGGING_TOPIC", "foo-log-consumer-group");

    private final String topicName;
    private final String consumerGroupId;

} 