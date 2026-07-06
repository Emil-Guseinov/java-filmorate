import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.model.Film.MAX_DESCRIPTION_LENGTH;

@SpringBootTest(classes = {
        ru.yandex.practicum.filmorate.service.UserService.class,
        ru.yandex.practicum.filmorate.storage.memory.InMemoryUserStorage.class,
        ru.yandex.practicum.filmorate.FilmorateApplication.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmControllerTest {
    private static Validator validator;
    private static ValidatorFactory factory;

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserService userService;

    @BeforeAll
    static void initValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    @DisplayName("Успешное создание фильма при корректных данных")
    void createValidFilm() {
        Film film = new Film();
        film.setName("Звездные войны.Эпизод 1:Скрытая угроза");
        film.setDescription("Галактические войны");
        film.setReleaseDate(LocalDate.of(1999, 5, 19));
        film.setDuration(136);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Валидный фильм должен проходить проверку без ошибок");
    }

    @Test
    @DisplayName("Не должен добавиться фильм при пробелах в имени")
    void createFilmWithEmptyName() {
        Film film = new Film();
        film.setName("     ");
        film.setDescription("Галактические войны");
        film.setReleaseDate(LocalDate.of(1999, 5, 19));
        film.setDuration(136);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());

        String message = violations.iterator().next().getMessage();
        assertEquals("Название фильма не должно быть пустым или содержать пробелы", message);
    }

    @Test
    @DisplayName("Не должен добавиться фильм при пустом имени")
    void createFilmWithNullName() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Галактические войны");
        film.setReleaseDate(LocalDate.of(1999, 5, 19));
        film.setDuration(136);


        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());

        String message = violations.iterator().next().getMessage();
        assertEquals("Название фильма не должно быть пустым или содержать пробелы", message);

    }

    @Test
    @DisplayName("Не должен добавиться фильм при пустом описании")
    void createFilmWithEmptyDescription() {
        Film film = new Film();
        film.setName("Звездные войны.Эпизод 1:Скрытая угроза");
        film.setDescription(" ");
        film.setReleaseDate(LocalDate.of(1999, 5, 19));
        film.setDuration(136);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());

        String message = violations.iterator().next().getMessage();
        assertEquals("Описание фильма должно быть заполнено", message);
    }

    @Test
    @DisplayName("Не должен пройти фильм при больше 200 символах в описании")
    void createFilmWithLengthMax200() {
        Film film = new Film();
        String maxDescription = "a".repeat(MAX_DESCRIPTION_LENGTH + 1);
        film.setName("Звездные войны.Эпизод 1:Скрытая угроза");
        film.setDescription(maxDescription);
        film.setReleaseDate(LocalDate.of(1999, 5, 19));
        film.setDuration(136);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());

        String message = violations.iterator().next().getMessage();
        assertEquals("Максимальная длина описания 200 символов", message);
    }

    @Test
    @DisplayName("Должен принять с описания фильма ровно 200 символов")
    void createFilmWithLength200() {
        Film film = new Film();
        String maxDescription = "a".repeat(MAX_DESCRIPTION_LENGTH);
        film.setName("Звездные войны.Эпизод 1:Скрытая угроза");
        film.setDescription(maxDescription);
        film.setReleaseDate(LocalDate.of(1999, 5, 19));
        film.setDuration(136);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);


        assertTrue(violations.isEmpty(), "Описание длиной ровно 200 символов должно успешно проходить валидацию");
    }

    @Test
    @DisplayName("Не должно пройти если дата релиза null")
    void createFilmWithReleaseDateNull() {
        Film film = new Film();
        film.setName("Звездные войны.Эпизод 1:Скрытая угроза");
        film.setDescription("Галактические войны");
        film.setReleaseDate(null);
        film.setDuration(136);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());

        String message = violations.iterator().next().getMessage();
        assertEquals("Дата релиза должна быть указана", message);
    }

    @Test
    @DisplayName("Дата релиза не должна быть раньше чем 1895-12-28")
    void createFilmWithReleaseDate() {
        Film film = new Film();
        film.setName("Звездные войны");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1700, 5, 19));
        film.setDuration(136);

        // Вызываем метод контроллера, так как логика проверки даты осталась в нём
        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> filmService.create(film)
        );
        assertEquals("Дата фильма должна быть не раньше 1895-12-28", exception.getMessage());
    }

    @Test
    @DisplayName("Проверка на продолжительность если оно меньше")
    void createFilmWithDuration() {
        Film film = new Film();
        film.setName("Звездные войны.Эпизод 1:Скрытая угроза");
        film.setDescription("Галактические войны");
        film.setReleaseDate(LocalDate.of(1999, 5, 19));
        film.setDuration(-5);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();
        assertEquals("Продолжительность фильма должна быть положительной", message);

    }

    @Test
    @DisplayName("Проверка на продолжительность если = 0")
    void createFilmWithDurationOfMinutes0() {
        Film film = new Film();
        film.setName("Звездные войны.Эпизод 1:Скрытая угроза");
        film.setDescription("Галактические войны");
        film.setReleaseDate(LocalDate.of(1999, 5, 19));
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();

        assertEquals("Продолжительность фильма должна быть положительной", message);

    }

    @Test
    @DisplayName("Должен обновить фильм успешно")
    void shouldUpdateExistingFilm() {

        Film film = new Film();
        film.setName("Звездные войны.Эпизод 1:Скрытая угроза");
        film.setDescription("Галактические войны");
        film.setReleaseDate(LocalDate.of(1999, 5, 19));
        film.setDuration(136);
        Film createdFilm = filmService.create(film);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName("Звездные войны.Эпизод 2:Атака клонов");
        updateFilm.setDescription("Галактические войны");
        updateFilm.setReleaseDate(LocalDate.of(2002, 5, 16));
        updateFilm.setDuration(142);

        Set<ConstraintViolation<Film>> violations = validator.validate(updateFilm);

        assertTrue(violations.isEmpty());

        Film testFilm = filmService.update(updateFilm);

        assertEquals("Звездные войны.Эпизод 2:Атака клонов", testFilm.getName());
        assertEquals(LocalDate.of(2002, 5, 16), testFilm.getReleaseDate());
        assertEquals(142, testFilm.getDuration());
    }

    @Test
    @DisplayName("Проверка id при обновлении фильма на null ")
    void shouldNotUpdateFilmWhenIdNull() {
        Film updateFilm = new Film();
        updateFilm.setId(null);
        updateFilm.setName("Звездные войны.Эпизод 2:Атака клонов");
        updateFilm.setDescription("Галактические войны");
        updateFilm.setReleaseDate(LocalDate.of(2002, 5, 16));
        updateFilm.setDuration(142);

        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> filmService.update(updateFilm)
        );
        assertEquals("id должен быть указан!", exception.getMessage());

    }

    @Test
    @DisplayName("Фильм не должен обновиться с не корректным Id")
    void shouldNotUpdateFilmWhenIdDoesNotExist() {
        Film updateFilm = new Film();
        updateFilm.setId(10L);
        updateFilm.setName("Звездные войны.Эпизод 2:Атака клонов");
        updateFilm.setDescription("Галактические войны");
        updateFilm.setReleaseDate(LocalDate.of(2002, 5, 16));
        updateFilm.setDuration(142);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmService.update(updateFilm)
        );
        assertEquals("id не найден", exception.getMessage());

    }

    @Test
    @DisplayName("Добавление лайка фильму (PUT /films/{id}/like/{userId})")
    void shouldAddLikeToFilm() {
        Film film = filmService.create(createFilmObject("Фильм 1", LocalDate.of(2000, 1, 1)));
        User user = userService.create(createUserObject("test@mail.ru", "login"));

        filmService.addLike(film.getId(), user.getId());

        Film updatedFilm = filmService.findById(film.getId());
        assertEquals(1, updatedFilm.getLikes().size(), "У фильма должен появиться 1 лайк");
        assertTrue(updatedFilm.getLikes().contains(user.getId()), "Сет лайков должен содержать ID пользователя");
    }

    @Test
    @DisplayName("Удаление лайка у фильма (DELETE /films/{id}/like/{userId})")
    void shouldDeleteLikeFromFilm() {
        Film film = filmService.create(createFilmObject("Фильм 1", LocalDate.of(2000, 1, 1)));
        User user = userService.create(createUserObject("test@mail.ru", "login"));

        filmService.addLike(film.getId(), user.getId());

        filmService.deleteLike(film.getId(), user.getId());

        Film updatedFilm = filmService.findById(film.getId());
        assertTrue(updatedFilm.getLikes().isEmpty(), "После удаления лайка список должен стать пустым");
    }

    @Test
    @DisplayName("Получение списка популярных фильмов по лайкам (GET /films/popular)")
    void shouldReturnPopularFilmsSorted() {
        Film f1 = filmService.create(createFilmObject("Менее популярный", LocalDate.of(2000, 1, 1)));
        Film f2 = filmService.create(createFilmObject("Самый популярный", LocalDate.of(2001, 1, 1)));

        User u1 = userService.create(createUserObject("u1@mail.ru", "u1"));
        User u2 = userService.create(createUserObject("u2@mail.ru", "u2"));

        filmService.addLike(f2.getId(), u1.getId());
        filmService.addLike(f2.getId(), u2.getId());

        filmService.addLike(f1.getId(), u1.getId());

        List<Film> popular = filmService.getTopFilms(10);

        assertEquals(2, popular.size(), "Должно вернуться 2 фильма");
        assertEquals(f2.getId(), popular.get(0).getId(), "Первым должен идти фильм с наибольшим количеством лайков");
        assertEquals(f1.getId(), popular.get(1).getId(), "Вторым должен идти фильм с меньшим количеством лайков");
    }

    @Test
    @DisplayName("Проверка работы параметра count для популярных фильмов")
    void shouldLimitPopularFilmsCount() {
        filmService.create(createFilmObject("Фильм 1", LocalDate.of(2000, 1, 1)));
        filmService.create(createFilmObject("Фильм 2", LocalDate.of(2001, 1, 1)));

        List<Film> popular = filmService.getTopFilms(1);

        assertEquals(1, popular.size(), "Список должен содержать только 1 фильм, так как передан count = 1");
    }

    private Film createFilmObject(String name, LocalDate releaseDate) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание фильма");
        film.setReleaseDate(releaseDate);
        film.setDuration(120);
        return film;
    }

    private User createUserObject(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Имя " + login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}