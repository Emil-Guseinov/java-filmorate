package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

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
}
