package com.stockmanager.dao;

import com.stockmanager.model.User;
import com.stockmanager.util.PasswordUtil;

import java.sql.*;
import java.util.Optional;

/**
 * Data Access Object for {@link User} entities.
 */
public class UserDAO {

    private final DatabaseManager db;

    public UserDAO() {
        this.db = DatabaseManager.getInstance();
    }

    UserDAO(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Finds a user by username.
     *
     * @param username the username to look up
     * @return an Optional containing the User if found, empty otherwise
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username", e);
        }
        return Optional.empty();
    }

    /**
     * Authenticates a user with the given credentials.
     *
     * @param username  the username
     * @param plainPassword the raw password
     * @return an Optional containing the authenticated User, or empty if credentials are invalid
     */
    public Optional<User> authenticate(String username, String plainPassword) {
        return findByUsername(username)
            .filter(u -> PasswordUtil.checkPassword(plainPassword, u.getPasswordHash()));
    }

    /**
     * Saves a new user to the database.
     *
     * @param user the user to insert (password must already be hashed)
     */
    public void save(User user) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            User.Role.valueOf(rs.getString("role"))
        );
    }
}
