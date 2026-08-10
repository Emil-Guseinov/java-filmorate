package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> usersAll() {
        return userStorage.usersAll();
    }

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка получить пользователя не удалась id пользователя {}", id);
                    return new NotFoundException("id пользователя " + id + " не найден");
                });
    }

    public User addFriend(Long id, Long friendId) {
        if (id.equals(friendId)) {
            throw new ConditionNotMetException("Добавление самого себя недопустимо");
        }
        User user = findById(id);
        User friend = findById(friendId);
        log.info("Пользователь {} добавляет в друзья {}", user.getName(), friend.getName());
        userStorage.addFriend(id, friendId);
        return user;
    }

    public User removeFriendId(Long id, Long friendId) {
        if (id.equals(friendId)) {
            throw new ConditionNotMetException("Удаление самого себя недопустимо");
        }
        User user = findById(id);
        User friend = findById(friendId);
        log.info("Пользователь {} удаляет из друзей {}", user.getName(), friend.getName());
        userStorage.removeFriend(id, friendId);
        return user;
    }

    public List<User> getFriends(Long id) {
        findById(id);
        return userStorage.getFriends(id);
    }

    public List<User> getOtherFriends(Long id, Long otherId) {
        findById(id);
        findById(otherId);
        return userStorage.getCommonFriends(id, otherId);
    }

    public User create(User user) {
        if (userStorage.containsLogin(user.getLogin())) {
            throw new ConditionNotMetException("Пользователь с таким логином уже зарегистрирован");
        }
        if (userStorage.containsEmail(user.getEmail())) {
            throw new ConditionNotMetException("Пользователь с таким email уже зарегистрирован");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.warn("Попытка обновления id null");
            throw new ConditionNotMetException("Id должен быть указан!");
        }
        User oldUser = userStorage.findById(user.getId())
                .orElseThrow(() -> {
                    log.warn("id {} еще не зарегистрирован", user.getId());
                    return new NotFoundException("Id " + user.getId() + " не существует");
                });

        String oldLogin = normalize(oldUser.getLogin());
        String newLogin = normalize(user.getLogin());
        if (!oldLogin.equals(newLogin) && userStorage.containsLogin(user.getLogin())) {
            throw new ConditionNotMetException("Логин занят");
        }

        String oldEmail = normalize(oldUser.getEmail());
        String newEmail = normalize(user.getEmail());
        if (!oldEmail.equals(newEmail) && userStorage.containsEmail(user.getEmail())) {
            throw new ConditionNotMetException("Этот email уже занят другим пользователем");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User updatedUser = userStorage.update(user);
        log.info("Пользователь обновлен name:{} ,id {} ,email:{}",
                updatedUser.getName(), updatedUser.getId(), updatedUser.getEmail());
        return updatedUser;
    }

    private String normalize(String value) {
        return value.toLowerCase().trim();
    }
}
