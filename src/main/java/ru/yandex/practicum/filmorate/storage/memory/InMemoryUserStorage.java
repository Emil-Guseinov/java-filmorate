package ru.yandex.practicum.filmorate.storage.memory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Set<String> usersEmail = ConcurrentHashMap.newKeySet();
    private final Set<String> usersLogin = ConcurrentHashMap.newKeySet();

    @Override
    public Collection<User> usersAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(Long id) {

        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User create(User user) {

        String loginKey = user.getLogin().toLowerCase().trim();
        if (usersLogin.contains(loginKey)) {
            throw new ConditionNotMetException("Пользователь с таким логином уже зарегистрирован");
        }

        String emailKey = user.getEmail().toLowerCase().trim();
        if (usersEmail.contains(emailKey)) {
            throw new ConditionNotMetException("Пользователь с таким email уже зарегистрирован");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        usersLogin.add(loginKey);
        usersEmail.add(emailKey);
        user.setId(getNextId());
        users.put(user.getId(), user);

        return user;
    }

    @Override
    public User update(User user) {

        User oldUser = users.get(user.getId());

        String oldLogin = oldUser.getLogin().toLowerCase().trim();
        String newLogin = user.getLogin().toLowerCase().trim();
        if (!oldLogin.equals(newLogin)) {
            usersLogin.remove(oldLogin);
            usersLogin.add(newLogin);
        }

        String oldEmail = oldUser.getEmail().toLowerCase().trim();
        String newEmail = user.getEmail().toLowerCase().trim();
        if (!oldEmail.equals(newEmail)) {
            usersEmail.remove(oldEmail);
            usersEmail.add(newEmail);
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public boolean containsLogin(String login) {
        return usersLogin.contains(login.toLowerCase().trim());
    }

    @Override
    public boolean containsEmail(String email) {
        return usersEmail.contains(email.toLowerCase().trim());
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        User user = users.get(id);
        User otherUser = users.get(otherId);

        if (user == null || otherUser == null) {
            return List.of();
        }

        return user.getFriends().stream()
                .filter(friendId -> otherUser.getFriends().contains(friendId))
                .map(users::get) //
                .toList();
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
