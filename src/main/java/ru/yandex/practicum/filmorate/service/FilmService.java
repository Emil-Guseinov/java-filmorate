package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> filmsAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {

        return filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка получить фильм не удалась id фильма {}", id);
                    return new NotFoundException("id фильма" + id + " не найден");
                });
    }

    public Film create(Film film) {

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без id");
            throw new ConditionNotMetException("id должен быть указан!");
        }

        Film oldFilm = filmStorage.findById(film.getId())
                .orElseThrow(() -> {
                    log.warn("Фильм с id {} не найден для обновления", film.getId());
                    return new NotFoundException("id не найден");
                });


        String oldKey = oldFilm.getName().toLowerCase().trim() + "_" + oldFilm.getReleaseDate();
        String newKey = film.getName().toLowerCase().trim() + "_" + film.getReleaseDate();

        if (!oldKey.equals(newKey)) {
            if (filmStorage.containsKey(newKey)) {
                log.warn("Попытка обновить фильм на уже существующий: {}", film.getName());
                throw new ConditionNotMetException("Фильм с таким названием и датой релиза уже существует");
            }
        }

        Film updatedFilm = filmStorage.update(film);
        log.info("Фильм обновлен: {}, id фильма {}", updatedFilm.getName(), updatedFilm.getId());
        return updatedFilm;
    }

    public List<Film> getTopFilms(Integer count) {

        if (count <= 0) {
            log.warn("Передано некорректное значение count: {}", count);
            throw new ConditionNotMetException("Количество фильмов должно быть больше 0");
        }

        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .toList();

    }

    public Film addLike(Long filmId, Long userId) {
        Film film = findById(filmId);

        if (userStorage.findById(userId).isEmpty()) {
            log.warn("Попытка поставить лайк у несуществующего пользователя с id {}", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        log.info("Пользователь с id {} ставит лайк у фильму {}", userId, film.getName());
        film.getLikes().add(userId);
        return film;
    }

    public Film deleteLike(Long filmId, Long userId) {
        Film film = findById(filmId);

        if (userStorage.findById(userId).isEmpty()) {
            log.warn("Попытка удалить лайк у несуществующего пользователя с id {}", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        log.info("Пользователь с id {} удаляет лайк у фильму {}", userId, film.getName());
        film.getLikes().remove(userId);
        return film;
    }
}