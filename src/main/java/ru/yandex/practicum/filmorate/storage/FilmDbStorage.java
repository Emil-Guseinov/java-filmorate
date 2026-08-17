package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final String BASE_FILM_SELECT =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                    "f.mpa_id, m.name AS mpa_name, m.description AS mpa_description " +
                    "FROM films AS f " +
                    "LEFT JOIN mpa_ratings AS m ON f.mpa_id = m.id";

    private static final FilmRowMapper FILM_ROW_MAPPER = new FilmRowMapper();

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbcTemplate.query(BASE_FILM_SELECT + " ORDER BY f.id", FILM_ROW_MAPPER);
        loadRelations(films);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        List<Film> films = jdbcTemplate.query(BASE_FILM_SELECT + " WHERE f.id = ?", FILM_ROW_MAPPER, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        loadRelations(films);
        return Optional.of(films.get(0));
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM films WHERE id = ?",
                Integer.class,
                id);
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public Film create(Film film) {
        String sqlQuery = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQuery, new String[]{"id"});
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, Date.valueOf(film.getReleaseDate()));
            statement.setInt(4, film.getDuration());
            if (film.getMpa() == null || film.getMpa().getId() == null) {
                statement.setNull(5, java.sql.Types.INTEGER);
            } else {
                statement.setLong(5, film.getMpa().getId());
            }
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            film.setId(key.longValue());
        }
        saveFilmGenres(film);
        return findById(film.getId()).orElse(film);
    }

    @Override
    @Transactional
    public Film update(Film film) {
        String sqlQuery = "UPDATE films " +
                "SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() == null ? null : film.getMpa().getId(),
                film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveFilmGenres(film);
        return findById(film.getId()).orElse(film);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update("MERGE INTO likes KEY(film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public List<Film> getTopFilms(int count) {
        String sqlQuery = BASE_FILM_SELECT +
                " LEFT JOIN (SELECT film_id, COUNT(*) AS likes_count FROM likes GROUP BY film_id) AS lc " +
                "ON f.id = lc.film_id " +
                "ORDER BY COALESCE(lc.likes_count, 0) DESC, f.id ASC LIMIT ?";
        List<Film> films = jdbcTemplate.query(sqlQuery, FILM_ROW_MAPPER, count);
        loadRelations(films);
        return films;
    }

    private void loadRelations(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        Map<Long, Film> filmsById = new LinkedHashMap<>();
        for (Film film : films) {
            film.getGenres().clear();
            film.getLikes().clear();
            filmsById.put(film.getId(), film);
        }

        List<Long> filmIds = new ArrayList<>(filmsById.keySet());
        String placeholders = String.join(", ", Collections.nCopies(filmIds.size(), "?"));
        Object[] args = filmIds.toArray();

        String genresSql = "SELECT fg.film_id, g.id, g.name " +
                "FROM film_genres AS fg " +
                "INNER JOIN genres AS g ON g.id = fg.genre_id " +
                "WHERE fg.film_id IN (" + placeholders + ") " +
                "ORDER BY fg.film_id, g.id";
        jdbcTemplate.query(genresSql, rs -> {
            Film film = filmsById.get(rs.getLong("film_id"));
            if (film != null) {
                Genre genre = new Genre();
                genre.setId(rs.getLong("id"));
                genre.setName(rs.getString("name"));
                film.getGenres().add(genre);
            }
        }, args);

        String likesSql = "SELECT film_id, user_id FROM likes " +
                "WHERE film_id IN (" + placeholders + ") " +
                "ORDER BY film_id, user_id";
        jdbcTemplate.query(likesSql, rs -> {
            Film film = filmsById.get(rs.getLong("film_id"));
            if (film != null) {
                film.getLikes().add(rs.getLong("user_id"));
            }
        }, args);
    }

    private void saveFilmGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = film.getGenres().stream()
                .filter(genre -> genre != null && genre.getId() != null)
                .map(Genre::getId)
                .distinct()
                .map(genreId -> new Object[]{film.getId(), genreId})
                .toList();

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "MERGE INTO film_genres KEY(film_id, genre_id) VALUES (?, ?)",
                    batchArgs);
        }
    }
}
