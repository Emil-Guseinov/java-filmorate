package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionNotMetException;
import ru.yandex.practicum.filmorate.exception.DelicateDateException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> usersAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {

        validate(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь name:{}  ,id {} ,email:{}", user.getName(), user.getId(), user.getEmail());

        return user;

    }

    @PutMapping
    public User update(@RequestBody User user) {

        if (user.getId() == null) {
            log.warn("Попытка обновления id null");
            throw new ConditionNotMetException("Id должен быть указан!");

        }
        if (!users.containsKey(user.getId())) {
            log.warn("id еще не зарегистрирован");
            throw new NotFoundException("Id не существует");

        }

        validate(user);
        users.put(user.getId(), user);
        log.info("Пользователь обновлен name:{}  ,id {} ,email:{}", user.getName(), user.getId(), user.getEmail());

        return user;
    }

    public Long getNextId() {
        long currentIdMax = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return currentIdMax + 1;
    }

    public void validate(User user) {

        if (user == null) {
            log.warn("Передан пустой объект null");
            throw new ConditionNotMetException("Тело запроса не должно быть пустым");

        }

        //Валидация имейла
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Получен null при попытке указании почты");
            throw new ConditionNotMetException("Электронная почта не может быть пустой");
        }
        boolean oldEmail = users.values()
                .stream()
                .filter(us -> us.getId() != null && !us.getId().equals(user.getId()))
                .anyMatch(us -> user.getEmail().equalsIgnoreCase(us.getEmail()));

        if (oldEmail) {
            log.warn("Указан уже использующий имейл {}", user.getEmail());
            throw new ConditionNotMetException("Имейл уже используется");
        }

        if (!user.getEmail().matches(EMAIL_PATTERN)) {
            log.warn("Указан не корректный формат почты {}", user.getEmail());
            throw new ConditionNotMetException("Не корректный формат электронной почты");
        }

        //Валидация логина
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Указан Login null ");
            throw new ConditionNotMetException("Логин не должен быть пустым");
        }
        boolean oldLogin = users.values()
                .stream()
                .filter(us -> us.getId() != null && !us.getId().equals(user.getId()))
                .anyMatch(us -> user.getLogin().equalsIgnoreCase(us.getLogin()));
        if (oldLogin) {
            log.warn("Указан уже использующийся логин {}", user.getLogin());
            throw new ConditionNotMetException("Логин занят");

        }


        //Валидация имя пользователя
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Пользователю дано имя в качестве логина {}", user.getName());

        }

        //Валидация даты рождения
        if (user.getBirthday() == null) {
            log.warn("При попытке указание даты рождение получен null");
            throw new ConditionNotMetException("Дата рождения должна быть указана");

        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Указана дата рождения в будущем {}", user.getBirthday());
            throw new DelicateDateException("Дата рождения не должна быть в будущем");

        }
    }
}