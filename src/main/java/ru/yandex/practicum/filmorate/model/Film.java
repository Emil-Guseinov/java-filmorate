package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.MinimumDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
public class Film {

    public static final int MAX_DESCRIPTION_LENGTH = 200;

    @JsonIgnore
    private final Set<Long> likes = new HashSet<>();

    private Long id;

    @NotBlank(message = "Название фильма не должно быть пустым или содержать пробелы")
    private String name;

    @NotBlank(message = "Описание фильма должно быть заполнено")
    @Size(max = MAX_DESCRIPTION_LENGTH, message = "Максимальная длина описания " + MAX_DESCRIPTION_LENGTH + " символов")
    private String description;

    @NotNull(message = "Дата релиза должна быть указана")
    @MinimumDate(message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;


}
