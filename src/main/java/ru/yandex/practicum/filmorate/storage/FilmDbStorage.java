package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Film> filmRowMapper = (ResultSet rs, int rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getObject("release_date", LocalDate.class));
        film.setDuration(rs.getInt("duration"));

        long mpaId = rs.getLong("mpa_id");
        if (!rs.wasNull()) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));
            mpa.setDescription(rs.getString("mpa_description"));
            film.setMpa(mpa);
        }
        return film;
    };

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> findAll() {
        String sqlQuery = baseFilmSelect() + " ORDER BY f.id";
        List<Film> films = jdbcTemplate.query(sqlQuery, filmRowMapper);
        films.forEach(this::loadRelations);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sqlQuery = baseFilmSelect() + " WHERE f.id = ?";
        List<Film> films = jdbcTemplate.query(sqlQuery, filmRowMapper, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        Film film = films.get(0);
        loadRelations(film);
        return Optional.of(film);
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
        String sqlQuery = "SELECT f.id " +
                "FROM films AS f " +
                "LEFT JOIN likes AS l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.user_id) DESC, f.id ASC " +
                "LIMIT ?";
        List<Long> ids = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> rs.getLong("id"), count);
        return ids.stream()
                .map(this::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    private String baseFilmSelect() {
        return "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "f.mpa_id, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films AS f " +
                "LEFT JOIN mpa_ratings AS m ON f.mpa_id = m.id";
    }

    private void loadRelations(Film film) {
        film.setGenres(getGenresByFilmId(film.getId()));
        film.getLikes().clear();
        film.getLikes().addAll(getLikesByFilmId(film.getId()));
    }

    private void saveFilmGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            if (genre != null && genre.getId() != null) {
                jdbcTemplate.update(
                        "MERGE INTO film_genres KEY(film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId());
            }
        }
    }

    private List<Genre> getGenresByFilmId(Long filmId) {
        String sqlQuery = "SELECT g.id, g.name " +
                "FROM genres AS g " +
                "INNER JOIN film_genres AS fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId);
    }

    private List<Long> getLikesByFilmId(Long filmId) {
        return jdbcTemplate.query(
                "SELECT user_id FROM likes WHERE film_id = ? ORDER BY user_id",
                (rs, rowNum) -> rs.getLong("user_id"), filmId);
    }
}
