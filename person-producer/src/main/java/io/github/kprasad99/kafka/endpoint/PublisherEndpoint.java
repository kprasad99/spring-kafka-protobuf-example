package io.github.kprasad99.kafka.endpoint;

import java.util.function.Function;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.kprasad99.kafka.model.Person;
import io.github.kprasad99.person.proto.PersonProto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublisherEndpoint {

    private final KafkaTemplate<Integer, PersonProto.Person> template;
    @lombok.NonNull
    private ModelMapper mapper;

    @PostMapping("/send/{id}")
    public Mono<Void> send(@NotBlank @PathVariable("id") int id, @Valid @RequestBody Person person) {

        io.github.kprasad99.person.proto.PersonProto.Person proto = toProto.apply(person).build();
        log.info("Sending message {} to kafka", proto);
        return Mono.create(sink -> template.send("kp.prod1", id, proto).addCallback(ok -> sink.success(), sink::error));
    }

    private Function<Person, PersonProto.Person.Builder> toProto = p -> mapper.map(p, PersonProto.Person.Builder.class);

}
