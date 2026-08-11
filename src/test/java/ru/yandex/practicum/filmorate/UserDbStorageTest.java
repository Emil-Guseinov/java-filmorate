package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Test
    void crudAndSearchMethodsWork() {
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
        assertThat(userStorage.findById(created.getId()).orElseThrow().getName()).isEqualTo("Новое имя");
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

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Имя " + login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
