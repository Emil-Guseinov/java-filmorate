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
        User user = findById(id);
        User otherUser = findById(otherId);

        return user.getFriends().stream()
                .filter(friendId -> otherUser.getFriends().contains(friendId))
                .map(this::findById)
                .toList();
    }

    public User create(User user) {

        return userStorage.create(user);

    }

    public User update(User user) {

        return userStorage.update(user);
    }
}