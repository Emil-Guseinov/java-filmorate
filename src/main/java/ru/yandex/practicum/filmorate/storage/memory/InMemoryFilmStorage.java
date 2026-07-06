package ru.yandex.practicum.filmorate.storage.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new ConcurrentHashMap<>();
    private final Set<String> filmsKey = ConcurrentHashMap.newKeySet();

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> findById(Long id) {

        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film create(Film film) {

        String uniqueKey = film.getName().toLowerCase().trim() + "_" + film.getReleaseDate();

        if (filmsKey.contains(uniqueKey)) {
            log.warn("Попытка создания такого же фильма");
            throw new ConditionNotMetException("Такой фильм уже добавлен");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        filmsKey.add(uniqueKey);

        log.info("добавлен фильм: {}, id фильма {}", film.getName(), film.getId());

        return film;

    }

    @Override
    public Film update(Film film) {

        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без id");
            throw new ConditionNotMetException("id должен быть указан!");
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
