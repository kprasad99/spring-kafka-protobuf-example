package io.github.kprasad99.kafka.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import io.github.kprasad99.person.proto.PersonProto;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic kpTopic(KafkaAdmin admin) {
        return new NewTopic("kp.prod1", 5, (short) 3);
    }

    @Bean
    public KafkaTemplate<Integer, PersonProto.Person> kafkaTemplate(ProducerFactory<Integer, PersonProto.Person> factory){
        return new KafkaTemplate<>(factory);
    }

}
