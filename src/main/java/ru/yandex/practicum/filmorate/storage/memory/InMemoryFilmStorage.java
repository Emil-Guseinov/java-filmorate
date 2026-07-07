package ru.yandex.practicum.filmorate.storage.memory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new ConcurrentHashMap<>();
    private final Set<String> filmsKey = ConcurrentHashMap.newKeySet();

    @Override
    public Collection<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());

        String uniqueKey = film.getName().toLowerCase().trim() + "_" + film.getReleaseDate();
        filmsKey.add(uniqueKey);
        films.put(film.getId(), film);

        return film;
    }

    @Override
    public Film update(Film film) {
        Film oldFilm = films.get(film.getId());

        String oldKey = oldFilm.getName().toLowerCase().trim() + "_" + oldFilm.getReleaseDate();
        String newKey = film.getName().toLowerCase().trim() + "_" + film.getReleaseDate();

        if (!oldKey.equals(newKey)) {
            filmsKey.remove(oldKey);
            filmsKey.add(newKey);
        }

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public boolean containsKey(String key) {
        return filmsKey.contains(key.toLowerCase().trim());
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
