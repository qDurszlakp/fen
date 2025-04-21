package com.sandbox.foo.kafka;

import com.sandbox.util.kafka.KafkaTopicInfo;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ProducerIntegrationTest extends AbstractKafkaIntegrationTest {

    private Consumer<String, String> testConsumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), KafkaTopicInfo.FOO_LOGGING.getConsumerGroupId(), "true");
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        testConsumer = cf.createConsumer();
        testConsumer.subscribe(java.util.Collections.singletonList(KafkaTopicInfo.FOO_LOGGING.getTopicName()));
    }

    @AfterEach
    void tearDown() {
        testConsumer.close();
    }

    @Test
    void shouldLogProducerProduceLogToKafkaTopic() {

        // given
        String expectedMessage = "[Foo] Random log message:";

        // when
        ConsumerRecord<String, String> receivedRecord = KafkaTestUtils.getSingleRecord(testConsumer, KafkaTopicInfo.FOO_LOGGING.getTopicName(), Duration.ofSeconds(10));

        // then
        assertThat(receivedRecord).isNotNull();
        assertThat(receivedRecord.value()).contains(expectedMessage);
    }
}