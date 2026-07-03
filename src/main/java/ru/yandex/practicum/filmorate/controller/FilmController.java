package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new ConcurrentHashMap<>();
    private final Set<String> filmsKey = ConcurrentHashMap.newKeySet();
    private static final LocalDate FILM_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    @GetMapping
    public Collection<Film> filmsAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {

        String uniqueKey = film.getName().toLowerCase().trim() + "_" + film.getReleaseDate();

        if (filmsKey.contains(uniqueKey)) {
            log.warn("Попытка создания такого же фильма");
            throw new ConditionNotMetException("Такой фильм уже добавлен");
        }

        if (film.getReleaseDate().isBefore(FILM_BIRTHDAY)) {
            log.warn("Дата фильма указана {} раньше чем {}", film.getReleaseDate(), FILM_BIRTHDAY);
            throw new ConditionNotMetException("Дата фильма должна быть не раньше " + FILM_BIRTHDAY);
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        filmsKey.add(uniqueKey);

        log.info("добавлен фильм: {}, id фильма {}", film.getName(), film.getId());

        return film;

    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {

        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без id");
            throw new ConditionNotMetException("id должен быть указан!");
        }
        if (film.getReleaseDate().isBefore(FILM_BIRTHDAY)) {
            log.warn("Дата фильма указана {} раньше чем {}", film.getReleaseDate(), FILM_BIRTHDAY);
            throw new ConditionNotMetException("Дата фильма должна быть не раньше " + FILM_BIRTHDAY);
        }

        Film oldFilm = films.get(film.getId());
        if (oldFilm == null) {
            log.warn("Фильм с id {} не найден", film.getId());
            throw new NotFoundException("id не найден");
        }

        String oldKey = oldFilm.getName().toLowerCase().trim() + "_" + oldFilm.getReleaseDate();
        String newKey = film.getName().toLowerCase().trim() + "_" + film.getReleaseDate();

        if (!oldKey.equals(newKey)) {
            if (filmsKey.contains(newKey)) {
                log.warn("Попытка обновить фильм на уже существующий: {}", film.getName());
                throw new ConditionNotMetException("Фильм с таким названием и датой релиза уже существует");
            }

            filmsKey.remove(oldKey);
            filmsKey.add(newKey);
        }

        films.put(film.getId(), film);

        log.info("Фильм обновлен: {}, id фильма {}", film.getName(), film.getId());
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return currentMaxId + 1;

    }
}