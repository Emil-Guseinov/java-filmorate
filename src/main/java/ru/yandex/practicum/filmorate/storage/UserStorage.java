package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface UserStorage {

    Collection<User> usersAll();

    Optional<User> findById(Long id);

    User create(User user);

    User update(User user);

    boolean containsLogin(String login);

    boolean containsEmail(String email);

    List<User> getCommonFriends(Long id, Long otherId);

}
