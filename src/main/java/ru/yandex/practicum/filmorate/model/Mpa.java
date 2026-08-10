package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Mpa {
    private Long id;
    private String name;

    @JsonIgnore
    private String description;
}
