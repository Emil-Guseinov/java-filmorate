package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Genre> findAll() {
        return jdbcTemplate.query(
                "SELECT id, name FROM genres ORDER BY id",
                (rs, rowNum) -> mapGenre(rs.getLong("id"), rs.getString("name")));
    }

    public Optional<Genre> findById(Long id) {
        List<Genre> genres = jdbcTemplate.query(
                "SELECT id, name FROM genres WHERE id = ?",
                (rs, rowNum) -> mapGenre(rs.getLong("id"), rs.getString("name")),
                id);
        return genres.stream().findFirst();
    }

    public Set<Long> findExistingIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        String placeholders = String.join(", ", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id FROM genres WHERE id IN (" + placeholders + ")";
        return new HashSet<>(jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id"),
                ids.toArray()));
    }

    private Genre mapGenre(long id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        return genre;
    }
}
