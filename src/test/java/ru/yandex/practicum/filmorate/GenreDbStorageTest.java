package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreStorage;

    @Test
    void returnsAllByIdAndChecksIdsInSingleQuery() {
        assertThat(genreStorage.findAll()).hasSize(6);
        assertThat(genreStorage.findById(1L).orElseThrow().getName()).isEqualTo("Комедия");
        assertThat(genreStorage.findById(999L)).isEmpty();
        assertThat(genreStorage.findExistingIds(List.of(1L, 3L, 999L)))
                .containsExactlyInAnyOrder(1L, 3L);
    }
}
