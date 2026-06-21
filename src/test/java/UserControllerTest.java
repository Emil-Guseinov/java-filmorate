import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.DelicateDateException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTest {
    UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    @DisplayName("Проверка на пустой запрос")
    void shouldThrowExceptionWhenUserIsNull() {
        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> userController.create(null));
        assertEquals("Тело запроса не должно быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("Должно пройти успешно при корректных данных")
    void createValidUser() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));

        User testUser = userController.create(user);

        assertEquals("testUnit@yandex.ru", testUser.getEmail());
        assertEquals("JAVA", testUser.getLogin());
        assertEquals("Джеймс Гослинг", testUser.getName());
        assertEquals(LocalDate.of(1995, 5, 23), testUser.getBirthday());

    }

    @Test
    @DisplayName("Проверка имейл на null")
    void createUserWithNullEmail() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));

        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> userController.create(user));
        assertEquals("Электронная почта не может быть пустой", exception.getMessage());
    }

    @Test
    @DisplayName("Проверка на уже существующий имейл")
    void duplicateEmailThrowsException() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));
        userController.create(user);

        User user2 = new User();
        user2.setEmail("testUnit@yandex.ru");
        user2.setLogin("JAV");
        user2.setName("Джеймс Роуз");
        user2.setBirthday(LocalDate.of(1990, 3, 20));

        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> userController.create(user2));
        assertEquals("Имейл уже используется", exception.getMessage());

    }

    @Test
    @DisplayName("Проверка на формат имейла")
    void createUserWithInvalidEmailFormat() {
        User user = new User();
        user.setEmail("@testUnityandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));

        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> userController.create(user));
        assertEquals("Не корректный формат электронной почты", exception.getMessage());
    }

    @Test
    @DisplayName("Должно не пройти если логин null")
    void createUserWithLoginNull() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin(null);
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));

        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> userController.create(user));
        assertEquals("Логин не должен быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("Должно не пройти если логин null")
    void createUserWithLoginBlank() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("        ");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));

        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> userController.create(user));
        assertEquals("Логин не должен быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("Не должен пройти логин который уже занят другим пользователем")
    void duplicateLoginUserThrowsException() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));
        userController.create(user);

        User user2 = new User();
        user2.setEmail("testMax@yandex.ru");
        user2.setLogin("JAVA");
        user2.setName("Rico Lucas");
        user2.setBirthday(LocalDate.of(1996, 5, 23));

        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> userController.create(user2));
        assertEquals("Логин занят", exception.getMessage());
    }

    @Test
    @DisplayName("Если имя null то пользователю дано имя в качестве логина")
    void createUserNameWithNullToLogin() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName(null);
        user.setBirthday(LocalDate.of(1995, 5, 23));

        User testUser = userController.create(user);

        assertEquals("JAVA", testUser.getName());

    }

    @Test
    @DisplayName("Проверка даты рождения на null")
    void createUserWithBirthdayNull() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(null);

        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> userController.create(user));
        assertEquals("Дата рождения должна быть указана", exception.getMessage());
    }

    @Test
    @DisplayName("Дата рождения в будущем")
    void createUserWithBirthdayInvalid() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(2060, 5, 23));

        DelicateDateException exception = assertThrows(DelicateDateException.class,
                () -> userController.create(user));

        assertEquals("Дата рождения не должна быть в будущем", exception.getMessage());
    }

    @Test
    @DisplayName("Должен успешно обновить пользователя")
    void shouldUpdateExistingUser() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));
        User user1 = userController.create(user);

        User user2 = new User();
        user2.setId(user1.getId());
        user2.setEmail("yandex-practicum@yandex.ru");
        user2.setLogin("Pat");
        user2.setName("Rico Mori");
        user2.setBirthday(LocalDate.of(1996, 12, 11));

        User testUser = userController.update(user2);

        assertEquals("yandex-practicum@yandex.ru", testUser.getEmail());
        assertEquals("Pat", testUser.getLogin());
        assertEquals("Rico Mori", testUser.getName());
        assertEquals(LocalDate.of(1996, 12, 11), testUser.getBirthday());
    }

    @Test
    @DisplayName("Проверка id на null")
    void shouldUpdateUserWithIdNull() {
        User user = new User();
        user.setId(null);
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));

        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> userController.update(user));
        assertEquals("Id должен быть указан!", exception.getMessage());
    }

    @Test
    @DisplayName("пользователь не должен обновится с не корректным id")
    void updateUserWithInvalidId() {
        User user = new User();
        user.setId(155L);
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.update(user));
        assertEquals("Id не существует", exception.getMessage());
    }
}
