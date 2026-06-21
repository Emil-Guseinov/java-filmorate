package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate FILM_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    @GetMapping
    public Collection<Film> filmsAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {

        validate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("добавлен фильм: {}, id фильма {}", film.getName(), film.getId());

        return film;

    }

    @PutMapping
    public Film update(@RequestBody Film film) {

        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без id");
            throw new ConditionNotMetException("id должен быть указан!");
        }
        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id {} не найден", film.getId());
            throw new NotFoundException("id не найден");
        }
        validate(film);
        films.put(film.getId(), film);

        log.info("Фильм обновлен: {}, id фильма {}", film.getName(), film.getId());
        return film;
    }

    public long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return currentMaxId + 1;

    }

    public void validate(Film film) {

        if (film == null) {
            log.warn("Передан пустой объект null");
            throw new ConditionNotMetException("Тело запроса не должно быть пустым");
        }

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Название фильма пустое или содержит пробелы ");
            throw new ConditionNotMetException("Название фильма не должно быть пустым или содержать пробелы");

        }

        //Валидация описания фильма
        if (film.getDescription() == null || film.getDescription().isBlank()) {
            log.warn("Получен в описании null или пробелы");
            throw new ConditionNotMetException("Описание фильма должно быть заполнено");

        }

        if (film.getDescription().length() > 200) {
            log.warn("слишком длинное описание фильма {}", film.getName());
            throw new ConditionNotMetException("Максимальная длина описания 200 символов");


        }

        //Валидация даты релиза и продолжительность фильма
        if (film.getReleaseDate() == null) {
            log.warn("Получен null в дате релиза");
            throw new ConditionNotMetException("Дата релиза должна быть указана");

        }

        if (film.getReleaseDate().isBefore(FILM_BIRTHDAY)) {
            log.warn("Дата фильма указана {} раньше чем {}", film.getReleaseDate(), FILM_BIRTHDAY);
            throw new ConditionNotMetException("Дата фильма должна быть не раньше " + FILM_BIRTHDAY);
        }

        if (film.getDuration() <= 0) {
            log.warn("Продолжительность фильма не положительная {}", film.getDuration());
            throw new ConditionNotMetException("Продолжительность фильма должна быть положительной");
        }
    }
}