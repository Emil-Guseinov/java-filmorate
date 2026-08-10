package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> filmsAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка получить фильм не удалась id фильма {}", id);
                    return new NotFoundException("id фильма " + id + " не найден");
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
        if (filmStorage.findById(film.getId()).isEmpty()) {
            throw new NotFoundException("id не найден");
        }
        Film updatedFilm = filmStorage.update(film);
        log.info("Фильм обновлен: {}, id фильма {}", updatedFilm.getName(), updatedFilm.getId());
        return updatedFilm;
    }

    public List<Film> getTopFilms(Integer count) {
        if (count == null || count <= 0) {
            throw new ConditionNotMetException("Количество фильмов должно быть больше 0");
        }
        return filmStorage.getTopFilms(count);
    }

    public Film addLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        filmStorage.addLike(filmId, userId);
        return findById(filmId);
    }

    public Film deleteLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        filmStorage.deleteLike(filmId, userId);
        return findById(filmId);
    }
}
