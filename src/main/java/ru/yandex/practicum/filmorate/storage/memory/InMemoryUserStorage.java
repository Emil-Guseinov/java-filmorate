package ru.yandex.practicum.filmorate.storage.memory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
        String loginKey = normalize(user.getLogin());
        if (usersLogin.contains(loginKey)) {
            throw new ConditionNotMetException("Пользователь с таким логином уже зарегистрирован");
        }
        String emailKey = normalize(user.getEmail());
        if (usersEmail.contains(emailKey)) {
            throw new ConditionNotMetException("Пользователь с таким email уже зарегистрирован");
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
        if (oldUser != null) {
            usersLogin.remove(normalize(oldUser.getLogin()));
            usersEmail.remove(normalize(oldUser.getEmail()));
        }
        usersLogin.add(normalize(user.getLogin()));
        usersEmail.add(normalize(user.getEmail()));
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public boolean containsLogin(String login) {
        return usersLogin.contains(normalize(login));
    }

    @Override
    public boolean containsEmail(String email) {
        return usersEmail.contains(normalize(email));
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        User user = users.get(id);
        if (user != null) {
            user.getFriends().add(friendId);
        }
    }

    @Override
    public void removeFriend(Long id, Long friendId) {
        User user = users.get(id);
        if (user != null) {
            user.getFriends().remove(friendId);
        }
    }

    @Override
    public List<User> getFriends(Long id) {
        User user = users.get(id);
        if (user == null) {
            return List.of();
        }
        return user.getFriends().stream().map(users::get).filter(java.util.Objects::nonNull).toList();
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        User user = users.get(id);
        User otherUser = users.get(otherId);
        if (user == null || otherUser == null) {
            return List.of();
        }
        return user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(users::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private String normalize(String value) {
        return value.toLowerCase().trim();
    }

    private long getNextId() {
        return users.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1;
    }
}
