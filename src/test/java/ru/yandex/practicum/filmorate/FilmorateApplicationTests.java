package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
class FilmorateApplicationTests {

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private GenreDbStorage genreStorage;

    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    void userStorageCrudAndSearchMethodsWork() {
        User user = createUser("mail@test.ru", "login");
        User created = userStorage.create(user);

        assertThat(created.getId()).isNotNull();
        assertThat(userStorage.findById(created.getId())).contains(created);
        assertThat(userStorage.usersAll()).extracting(User::getId).contains(created.getId());
        assertThat(userStorage.containsEmail("MAIL@test.ru")).isTrue();
        assertThat(userStorage.containsLogin("LOGIN")).isTrue();

        created.setName("Новое имя");
        User updated = userStorage.update(created);
        assertThat(updated.getName()).isEqualTo("Новое имя");
        User reloaded = userStorage.findById(created.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Новое имя");
    }

    @Test
    void friendshipMethodsAreOneDirectionalAndCommonFriendsWork() {
        User first = userStorage.create(createUser("first@test.ru", "first"));
        User second = userStorage.create(createUser("second@test.ru", "second"));
        User common = userStorage.create(createUser("common@test.ru", "common"));

        userStorage.addFriend(first.getId(), second.getId());
        assertThat(userStorage.getFriends(first.getId())).extracting(User::getId).containsExactly(second.getId());
        assertThat(userStorage.getFriends(second.getId())).isEmpty();

        userStorage.addFriend(first.getId(), common.getId());
        userStorage.addFriend(second.getId(), common.getId());
        assertThat(userStorage.getCommonFriends(first.getId(), second.getId()))
                .extracting(User::getId).containsExactly(common.getId());

        userStorage.removeFriend(first.getId(), second.getId());
        assertThat(userStorage.getFriends(first.getId())).extracting(User::getId).containsExactly(common.getId());
    }

    @Test
    void filmStorageCrudMethodsWorkWithMpaAndGenres() {
        Film film = createFilm("Первый фильм", 1L, List.of(1L, 2L));
        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getMpa()).isNotNull();
        assertThat(created.getMpa().getName()).isEqualTo("G");
        assertThat(created.getGenres()).extracting(Genre::getName).containsExactly("Комедия", "Драма");
        assertThat(filmStorage.findById(created.getId())).isPresent();
        assertThat(filmStorage.findAll()).extracting(Film::getId).contains(created.getId());

        created.setName("Обновлённый фильм");
        created.setGenres(List.of(genreWithId(6L)));
        Film updated = filmStorage.update(created);
        assertThat(updated.getName()).isEqualTo("Обновлённый фильм");
        assertThat(updated.getGenres()).extracting(Genre::getName).containsExactly("Боевик");
    }

    @Test
    void filmLikesAndPopularMethodsWork() {
        Film first = filmStorage.create(createFilm("Первый", 1L, List.of()));
        Film second = filmStorage.create(createFilm("Второй", 2L, List.of()));
        User firstUser = userStorage.create(createUser("one@test.ru", "one"));
        User secondUser = userStorage.create(createUser("two@test.ru", "two"));

        filmStorage.addLike(second.getId(), firstUser.getId());
        filmStorage.addLike(second.getId(), secondUser.getId());
        filmStorage.addLike(first.getId(), firstUser.getId());

        Film secondReloaded = filmStorage.findById(second.getId()).orElseThrow();
        assertThat(secondReloaded.getLikes()).hasSize(2);
        assertThat(filmStorage.getTopFilms(2)).extracting(Film::getId)
                .containsExactly(second.getId(), first.getId());

        filmStorage.deleteLike(second.getId(), firstUser.getId());
        secondReloaded = filmStorage.findById(second.getId()).orElseThrow();
        assertThat(secondReloaded.getLikes()).hasSize(1);
    }

    @Test
    void genreStorageReturnsAllAndById() {
        assertThat(genreStorage.findAll()).hasSize(6);
        assertThat(genreStorage.findById(1L).orElseThrow().getName()).isEqualTo("Комедия");
        assertThat(genreStorage.findById(999L)).isEmpty();
    }

    @Test
    void mpaStorageReturnsExactlyFiveRatingsAndById() {
        assertThat(mpaStorage.findAll()).hasSize(5)
                .extracting(Mpa::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
        assertThat(mpaStorage.findById(1L).orElseThrow().getName()).isEqualTo("G");
        assertThat(mpaStorage.findById(999L)).isEmpty();
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Имя " + login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    private Film createFilm(String name, Long mpaId, List<Long> genreIds) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(mpaId);
        film.setMpa(mpa);
        film.setGenres(genreIds.stream().map(this::genreWithId).toList());
        return film;
    }

    private Genre genreWithId(Long id) {
        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }
}
