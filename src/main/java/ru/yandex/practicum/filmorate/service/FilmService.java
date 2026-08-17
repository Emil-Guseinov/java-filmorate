package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreDbStorage genreStorage,
                       MpaDbStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
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
        validateReferences(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без id");
            throw new ConditionNotMetException("id должен быть указан!");
        }
        if (!filmStorage.existsById(film.getId())) {
            throw new NotFoundException("id не найден");
        }
        validateReferences(film);
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

    public void addLike(Long filmId, Long userId) {
        ensureFilmAndUserExist(filmId, userId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        ensureFilmAndUserExist(filmId, userId);
        filmStorage.deleteLike(filmId, userId);
    }

    private void ensureFilmAndUserExist(Long filmId, Long userId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("id фильма " + filmId + " не найден");
        }
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private void validateReferences(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null
                && mpaStorage.findById(film.getMpa().getId()).isEmpty()) {
            throw new NotFoundException("Рейтинг MPA с id " + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        Set<Long> genreIds = new LinkedHashSet<>();
        for (Genre genre : film.getGenres()) {
            if (genre != null && genre.getId() != null) {
                genreIds.add(genre.getId());
            }
        }

        Set<Long> existingIds = genreStorage.findExistingIds(genreIds);
        for (Long genreId : genreIds) {
            if (!existingIds.contains(genreId)) {
                throw new NotFoundException("Жанр с id " + genreId + " не найден");
            }
        }
    }
}
