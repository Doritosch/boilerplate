package dev.minsu.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;

@Entity
public class SampleEntity {

    @Id @GeneratedValue
    private Long id;

    private String name;

}
