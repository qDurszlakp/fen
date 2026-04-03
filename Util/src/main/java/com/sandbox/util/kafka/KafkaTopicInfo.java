package com.sandbox.util.kafka;

import lombok.Getter;

public enum KafkaTopicInfo {

    FOO_LOGGING("FOO_LOGGING_TOPIC", "foo-log-consumer-group");

    public String getTopicName() {
        return topicName;
    }

    public String getConsumerGroupId() {
        return consumerGroupId;
    }

    private final String topicName;
    private final String consumerGroupId;

    KafkaTopicInfo(String topicName, String consumerGroupId) {
        this.topicName = topicName;
        this.consumerGroupId = consumerGroupId;
    }

} 