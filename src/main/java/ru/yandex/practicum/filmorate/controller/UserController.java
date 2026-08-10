package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> usersAll() {
        return userService.usersAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {

        return userService.findById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getOtherFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getOtherFriends(id, otherId);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {

        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {

        return userService.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {

        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriendId(@PathVariable Long id, @PathVariable Long friendId) {
        return userService.removeFriendId(id, friendId);
    }
}