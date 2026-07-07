package ru.yandex.practicum.filmorate.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;

    private String name;

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Не корректный формат электронной почты")
    private String email;

    @NotBlank(message = "Логин не должен быть пустым")
    private String login;

    @NotNull(message = "Дата рождения должна быть указана")
    @PastOrPresent(message = "Дата рождения не должна быть в будущем")
    private LocalDate birthday;

    @JsonIgnore
    private Set<@Positive Long> friends = new HashSet<>();
}
