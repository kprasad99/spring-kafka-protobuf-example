package io.github.kprasad99.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import io.github.kprasad99.kafka.endpoint.PublisherEndpoint;
import io.github.kprasad99.kafka.model.Person;
import io.github.kprasad99.kafka.serde.KafkaProtobufDeserializer;
import io.github.kprasad99.person.proto.PersonProto;

@EmbeddedKafka(bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
public class KafkaProducerApplicationTests {

    private static final String TOPIC = "kp.prod1";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private PublisherEndpoint endpoint;

    @Autowired
    private KafkaTemplate<Integer, PersonProto.Person> sender;

    private BlockingQueue<ConsumerRecord<Integer, PersonProto.Person>> records;

    private KafkaMessageListenerContainer<Integer, PersonProto.Person> container;

    @BeforeAll
    public void setUp() {
        System.setProperty("spring.kafka.bootstrap-servers", embeddedKafkaBroker.getBrokersAsString());
        Map<String, Object> configs = new HashMap<>(
                KafkaTestUtils.consumerProps("consumer", "true", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<Integer, PersonProto.Person> consumerFactory = new DefaultKafkaConsumerFactory<>(
                configs, new IntegerDeserializer(), new KafkaProtobufDeserializer<>());
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<Integer, PersonProto.Person>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @AfterAll
    public void tearDown() {
        container.stop();
    }

    @Test
    public void testSend() {
        Person person = new Person();
        person.setId(1);
        person.setFirstName("kprasad");
        person.setLastName("ad");
        person.setAge(10);
        endpoint.send(1, person).doOnError(e -> System.out.println("e::" + e)).subscribe();
        sender.flush();
        Awaitility.await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(50)).untilAsserted(() -> {
            ConsumerRecord<Integer, PersonProto.Person> oneRecord = records.poll();
            assertThat(oneRecord == null ? "" : oneRecord.value().getFirstName()).isEqualTo("karthik");
        });
    }

}
