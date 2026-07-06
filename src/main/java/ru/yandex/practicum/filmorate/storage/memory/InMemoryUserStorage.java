package ru.yandex.practicum.filmorate.storage.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Set<String> usersEmail = ConcurrentHashMap.newKeySet();
    private final Set<String> usersLogin = ConcurrentHashMap.newKeySet();

    @Override
    public Collection<User> usersAll() {
        return users.values();
    }

    @Override
    public Optional<User> findById(Long id) {

        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User create(User user) {

        String loginKey = user.getLogin().toLowerCase().trim();
        if (usersLogin.contains(loginKey)) {
            log.warn("Попытка регистрации пользователя с уже существующим логином");
            throw new ConditionNotMetException("Пользователь с таким логином уже зарегистрирован");
        }

        String emailKey = user.getEmail().toLowerCase().trim();
        if (usersEmail.contains(emailKey)) {
            log.warn("Попытка регистрации пользователя с уже существующим email: {}", user.getEmail());
            throw new ConditionNotMetException("Пользователь с таким email уже зарегистрирован");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Пользователю дано имя в качестве логина {}", user.getName());

        }

        usersLogin.add(loginKey);
        usersEmail.add(emailKey);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь name:{}  ,id {} ,email:{}", user.getName(), user.getId(), user.getEmail());

        return user;
    }

    @Override
    public User update(User user) {

        if (user.getId() == null) {
            log.warn("Попытка обновления id null");
            throw new ConditionNotMetException("Id должен быть указан!");

        }

        User oldUser = users.get(user.getId());
        if (oldUser == null) {
            log.warn("id {} еще не зарегистрирован", user.getId());
            throw new NotFoundException("Id " + user.getId() + " не существует");

        }

        String oldLogin = oldUser.getLogin().toLowerCase().trim();
        String newLogin = user.getLogin().toLowerCase().trim();
        if (!oldLogin.equals(newLogin)) {
            if (usersLogin.contains(newLogin)) {
                log.warn("Указан уже использующийся логин {}", user.getLogin());
                throw new ConditionNotMetException("Логин занят");
            }
            usersLogin.remove(oldLogin);
            usersLogin.add(newLogin);
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String oldEmail = oldUser.getEmail().toLowerCase().trim();
        String newEmail = user.getEmail().toLowerCase().trim();

        if (!oldEmail.equals(newEmail)) {
            if (usersEmail.contains(newEmail)) {
                log.warn("Попытка изменить email на уже существующий: {}", user.getEmail());
                throw new ConditionNotMetException("Этот email уже занят другим пользователем");
            }
            usersEmail.remove(oldEmail);
            usersEmail.add(newEmail);
        }

        users.put(user.getId(), user);
        log.info("Пользователь обновлен name:{}  ,id {} ,email:{}", user.getName(), user.getId(), user.getEmail());

        return user;
    }


    private Long getNextId() {
        long currentIdMax = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return currentIdMax + 1;
    }
}
