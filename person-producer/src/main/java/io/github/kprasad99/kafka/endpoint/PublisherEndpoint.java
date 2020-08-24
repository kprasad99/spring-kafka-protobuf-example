package io.github.kprasad99.kafka.endpoint;

import javax.validation.constraints.NotBlank;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping("/api")
@AllArgsConstructor
public class PublisherEndpoint {

    private final KafkaTemplate<String, String> template;

    @GetMapping("/send")
    public Mono<Void> send(@NotBlank @RequestParam("key") String key, @NotBlank @RequestParam("data") String data) {

        log.info("Sending message {} to kafka", data);
        return Mono.create(sink -> template.send("kp.prod1", key, data).addCallback(ok -> sink.success(), sink::error));
    }
}
