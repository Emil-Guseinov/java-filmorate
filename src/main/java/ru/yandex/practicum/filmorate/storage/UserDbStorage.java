package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = (ResultSet rs, int rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getObject("birthday", LocalDate.class));
        return user;
    };

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> usersAll() {
        String sqlQuery = "SELECT id, email, login, name, birthday FROM \"USERS\" ORDER BY id";
        return jdbcTemplate.query(sqlQuery, userRowMapper);
    }

    @Override
    public Optional<User> findById(Long id) {
        String sqlQuery = "SELECT id, email, login, name, birthday FROM \"USERS\" WHERE id = ?";
        List<User> users = jdbcTemplate.query(sqlQuery, userRowMapper, id);
        return users.stream().findFirst();
    }

    @Override
    public User create(User user) {
        String sqlQuery = "INSERT INTO \"USERS\" (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQuery, new String[]{"id"});
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());
            statement.setDate(4, Date.valueOf(user.getBirthday()));
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            user.setId(key.longValue());
        }
        return user;
    }

    @Override
    public User update(User user) {
        String sqlQuery = "UPDATE \"USERS\" SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    @Override
    public boolean containsLogin(String login) {
        String sqlQuery = "SELECT COUNT(*) FROM \"USERS\" WHERE LOWER(login) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sqlQuery, Integer.class, login.trim());
        return count != null && count > 0;
    }

    @Override
    public boolean containsEmail(String email) {
        String sqlQuery = "SELECT COUNT(*) FROM \"USERS\" WHERE LOWER(email) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sqlQuery, Integer.class, email.trim());
        return count != null && count > 0;
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        String sqlQuery = "MERGE INTO friendships KEY(user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, id, friendId);
    }

    @Override
    public void removeFriend(Long id, Long friendId) {
        String sqlQuery = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sqlQuery, id, friendId);
    }

    @Override
    public List<User> getFriends(Long id) {
        String sqlQuery = "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                "FROM \"USERS\" AS u " +
                "INNER JOIN friendships AS f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? " +
                "ORDER BY u.id";
        return jdbcTemplate.query(sqlQuery, userRowMapper, id);
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        String sqlQuery = "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                "FROM \"USERS\" AS u " +
                "INNER JOIN friendships AS f1 ON u.id = f1.friend_id " +
                "INNER JOIN friendships AS f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ? " +
                "ORDER BY u.id";
        return jdbcTemplate.query(sqlQuery, userRowMapper, id, otherId);
    }
}
