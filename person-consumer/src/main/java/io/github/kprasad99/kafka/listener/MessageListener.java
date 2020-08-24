package io.github.kprasad99.kafka.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MessageListener {


    @KafkaListener(topics = "kp.prod1")
    public void listen(@Payload String data) {
        log.info("Recieved message {} from kafka", data);
    }
}
