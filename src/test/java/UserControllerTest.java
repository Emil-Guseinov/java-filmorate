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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        ru.yandex.practicum.filmorate.service.UserService.class,
        ru.yandex.practicum.filmorate.storage.memory.InMemoryUserStorage.class,
        ru.yandex.practicum.filmorate.FilmorateApplication.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerTest {
    private static Validator validator;
    private static ValidatorFactory factory;

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
    @DisplayName("Должно пройти успешно при корректных данных")
    void createValidUser() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Должен пройти проверку без ошибок");

    }

    @Test
    @DisplayName("Проверка имейл на null")
    void createUserWithNullEmail() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());

        String message = violations.iterator().next().getMessage();
        assertEquals("Электронная почта не может быть пустой", message);
    }

    @Test
    @DisplayName("Проверка на уже существующий имейл")
    void duplicateEmailThrowsException() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));
        userService.create(user);

        User user2 = new User();
        user2.setEmail("testUnit@yandex.ru");
        user2.setLogin("JAV");
        user2.setName("Джеймс Роуз");
        user2.setBirthday(LocalDate.of(1990, 3, 20));

        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> userService.create(user2));
        assertEquals("Пользователь с таким email уже зарегистрирован", exception.getMessage());

    }

    @Test
    @DisplayName("Проверка на формат имейла")
    void createUserWithInvalidEmailFormat() {
        User user = new User();
        user.setEmail("@testUnityandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());

        String message = violations.iterator().next().getMessage();
        assertEquals("Не корректный формат электронной почты", message);
    }

    @Test
    @DisplayName("Должно не пройти если логин null")
    void createUserWithLoginNull() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin(null);
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());

        String message = violations.iterator().next().getMessage();
        assertEquals("Логин не должен быть пустым", message);
    }

    @Test
    @DisplayName("Не должен пройти логин который уже занят другим пользователем")
    void duplicateLoginUserThrowsException() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));
        userService.create(user);

        User user2 = new User();
        user2.setEmail("testMax@yandex.ru");
        user2.setLogin("JAVA");
        user2.setName("Rico Lucas");
        user2.setBirthday(LocalDate.of(1996, 5, 23));

        ConditionNotMetException exception = assertThrows(ConditionNotMetException.class,
                () -> userService.create(user2));
        assertEquals("Пользователь с таким логином уже зарегистрирован", exception.getMessage());
    }

    @Test
    @DisplayName("Если имя null то пользователю дано имя в качестве логина")
    void createUserNameWithNullToLogin() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName(null);
        user.setBirthday(LocalDate.of(1995, 5, 23));

        User testUser = userService.create(user);

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

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());

        String message = violations.iterator().next().getMessage();
        assertEquals("Дата рождения должна быть указана", message);
    }

    @Test
    @DisplayName("Дата рождения в будущем")
    void createUserWithBirthdayInvalid() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(2060, 5, 23));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());

        String message = violations.iterator().next().getMessage();
        assertEquals("Дата рождения не должна быть в будущем", message);
    }

    @Test
    @DisplayName("Должен успешно обновить пользователя")
    void shouldUpdateExistingUser() {
        User user = new User();
        user.setEmail("testUnit@yandex.ru");
        user.setLogin("JAVA");
        user.setName("Джеймс Гослинг");
        user.setBirthday(LocalDate.of(1995, 5, 23));
        User user1 = userService.create(user);

        User user2 = new User();
        user2.setId(user1.getId());
        user2.setEmail("yandex-practicum@yandex.ru");
        user2.setLogin("Pat");
        user2.setName("Rico Mori");
        user2.setBirthday(LocalDate.of(1996, 12, 11));

        Set<ConstraintViolation<User>> violations = validator.validate(user2);

        assertTrue(violations.isEmpty(), "Должно пройти проверку");

        User updatedUser = userService.update(user2);
        assertEquals("Pat", updatedUser.getLogin(), "Логин должен успешно обновиться");
        assertEquals("Rico Mori", updatedUser.getName(), "Имя должно успешно обновиться");
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
                () -> userService.update(user));
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
                () -> userService.update(user));
        assertEquals("Id 155 не существует", exception.getMessage());
    }

    @Test
    @DisplayName("Добавление в друзья и получение списка друзей")
    void addFriendAndGetFriendsList() {
        User user1 = userService.create(createUserObject("one@mail.ru", "login1"));
        User user2 = userService.create(createUserObject("two@mail.ru", "login2"));

        userService.addFriend(user1.getId(), user2.getId());

        List<User> friendsOfUser1 = userService.getFriends(user1.getId());
        List<User> friendsOfUser2 = userService.getFriends(user2.getId());

        assertEquals(1, friendsOfUser1.size(), "У первого пользователя должен быть 1 друг");
        assertEquals(user2.getId(), friendsOfUser1.getFirst().getId(), "Другом должен быть пользователь 2");

        assertEquals(1, friendsOfUser2.size(), "У второго пользователя тоже должен быть 1 друг");
        assertEquals(user1.getId(), friendsOfUser2.getFirst().getId(), "Другом должен быть пользователь 1");
    }

    @Test
    @DisplayName("Удаление из друзей")
    void removeFriend() {
        User user1 = userService.create(createUserObject("one@mail.ru", "login1"));
        User user2 = userService.create(createUserObject("two@mail.ru", "login2"));

        userService.addFriend(user1.getId(), user2.getId());

        userService.removeFriendId(user1.getId(), user2.getId());

        assertTrue(userService.getFriends(user1.getId()).isEmpty(), "Список друзей пользователя 1 должен стать пустым");
        assertTrue(userService.getFriends(user2.getId()).isEmpty(), "Список друзей пользователя 2 должен стать пустым");
    }

    @Test
    @DisplayName("Получение списка общих друзей")
    void getCommonFriends() {
        User user1 = userService.create(createUserObject("one@mail.ru", "login1"));
        User user2 = userService.create(createUserObject("two@mail.ru", "login2"));
        User commonFriend = userService.create(createUserObject("common@mail.ru", "common"));

        userService.addFriend(user1.getId(), commonFriend.getId());
        userService.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userService.getOtherFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size(), "Должен быть ровно один общий друг");
        assertEquals(commonFriend.getId(), commonFriends.getFirst().getId(), "Общим другом должен быть пользователь common");
    }

    private User createUserObject(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Name " + login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}