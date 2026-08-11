package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    void returnsExactlyFiveRatingsAndById() {
        assertThat(mpaStorage.findAll()).hasSize(5)
                .extracting(Mpa::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
        assertThat(mpaStorage.findById(1L).orElseThrow().getName()).isEqualTo("G");
        assertThat(mpaStorage.findById(999L)).isEmpty();
    }
}
