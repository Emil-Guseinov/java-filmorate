package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> findAll() {
        return jdbcTemplate.query(
                "SELECT id, name, description FROM mpa_ratings ORDER BY id",
                (rs, rowNum) -> mapMpa(rs.getLong("id"), rs.getString("name"), rs.getString("description")));
    }

    public Optional<Mpa> findById(Long id) {
        List<Mpa> ratings = jdbcTemplate.query(
                "SELECT id, name, description FROM mpa_ratings WHERE id = ?",
                (rs, rowNum) -> mapMpa(rs.getLong("id"), rs.getString("name"), rs.getString("description")),
                id);
        return ratings.stream().findFirst();
    }

    private Mpa mapMpa(long id, String name, String description) {
        Mpa mpa = new Mpa();
        mpa.setId(id);
        mpa.setName(name);
        mpa.setDescription(description);
        return mpa;
    }
}
