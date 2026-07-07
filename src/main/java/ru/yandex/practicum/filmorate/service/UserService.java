package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> usersAll() {
        return userStorage.usersAll();
    }

    public User findById(Long id) {

        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка получить пользователя не удалась id пользователя {}", id);
                    return new NotFoundException("id пользователя" + id + " не найден");
                });

    }

    public User addFriend(Long id, Long friendId) {

        if (id.equals(friendId)) {
            throw new ConditionNotMetException("Добавление самого себя недопустимо");
        }
        User user = findById(id);
        User friend = findById(friendId);

        log.info("Пользователь {} добавляет в друзья {}", user.getName(), friend.getName());
        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        return user;
    }

    public User removeFriendId(Long id, Long friendId) {

        if (id.equals(friendId)) {
            throw new ConditionNotMetException("Удаление самого себя недопустимо");
        }
        User user = findById(id);
        User friend = findById(friendId);

        log.info("Пользователь {} удаляет из друзей {}", user.getName(), friend.getName());
        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        return user;
    }

    public List<User> getFriends(Long id) {
        User user = findById(id);

        return user.getFriends().stream()
                .map(this::findById)
                .toList();
    }

    public List<User> getOtherFriends(Long id, Long otherId) {

        userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
        userStorage.findById(otherId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + otherId + " не найден"));

        return userStorage.getCommonFriends(id, otherId);
    }

    public User create(User user) {

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

        String oldLogin = oldUser.getLogin().toLowerCase().trim();
        String newLogin = user.getLogin().toLowerCase().trim();
        if (!oldLogin.equals(newLogin)) {
            if (userStorage.containsLogin(newLogin)) {
                log.warn("Указан уже использующийся логин {}", user.getLogin());
                throw new ConditionNotMetException("Логин занят");
            }
        }

        String oldEmail = oldUser.getEmail().toLowerCase().trim();
        String newEmail = user.getEmail().toLowerCase().trim();
        if (!oldEmail.equals(newEmail)) {
            if (userStorage.containsEmail(newEmail)) {
                log.warn("Попытка изменить email на уже существующий: {}", user.getEmail());
                throw new ConditionNotMetException("Этот email уже занят другим пользователем");
            }
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User updatedUser = userStorage.update(user);

        log.info("Пользователь обновлен name:{} ,id {} ,email:{}", updatedUser.getName(), updatedUser.getId(), updatedUser.getEmail());

        return updatedUser;

    }
}