package com.sandbox.server.kafka;

import com.sandbox.BasicInfrastructure;
import com.sandbox.util.kafka.KafkaTopicInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public class ConsumerIntegrationTest extends BasicInfrastructure {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void shouldFooLogConsumerConsumeMessageFromTopic(CapturedOutput output) {

        // given
        String messageToSend = "Hello Test!";
        String topicName = KafkaTopicInfo.FOO_LOGGING.getTopicName();
        String expectedLogMessage = "[Server] Received message from topic " + topicName + " (partition 0): " + messageToSend;

        // when
        kafkaTemplate.send(topicName, messageToSend);

        // then
        await().atMost(Duration.ofSeconds(10))
               .pollInterval(Duration.ofMillis(100))
               .untilAsserted(() -> assertThat(output.getAll()).contains(expectedLogMessage));
    }

}
