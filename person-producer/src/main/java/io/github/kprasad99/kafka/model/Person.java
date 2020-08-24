package io.github.kprasad99.kafka.model;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class Person {

    @NotNull
    private int id;
    private String firstName;
    private String lastName;
    private int age;

}
