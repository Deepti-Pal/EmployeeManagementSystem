package com.ems.dao;

import com.ems.model.User;
import com.ems.util.DatabaseConnection;
import com.ems.util.PasswordUtil;

import java.sql.*;

/**
 * UserDAO - Handles login authentication and user management.
 */
public class UserDAO {

    private static final String FIND_BY_USERNAME =
        "SELECT * FROM users WHERE username=? AND is_active=TRUE";

    private static final String UPDATE_LAST_LOGIN =
        "UPDATE users SET last_login=NOW() WHERE user_id=?";

    private static final String INSERT_USER =
        "INSERT INTO users (username, password_hash, role, emp_id) VALUES (?,?,?,?)";

    private static final String CHANGE_PASSWORD =
        "UPDATE users SET password_hash=? WHERE user_id=?";

    /**
     * Authenticates a user by username + plain-text password.
     * @return the User object on success, null on failure
     */
    public User authenticate(String username, String plainPassword) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_USERNAME)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {
                    User user = mapRow(rs);
                    // Record login timestamp
                    try (PreparedStatement ps2 = conn.prepareStatement(UPDATE_LAST_LOGIN)) {
                        ps2.setInt(1, user.getUserId());
                        ps2.executeUpdate();
                    }
                    System.out.println("[Auth] Login successful: " + username + " (" + user.getRole() + ")");
                    return user;
                }
            }
            System.out.println("[Auth] Login failed for: " + username);
        } catch (SQLException e) {
            System.err.println("[DAO] authenticate error: " + e.getMessage());
        }
        return null;
    }

    public boolean createUser(User user, String plainPassword) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtil.hashPassword(plainPassword));
            ps.setString(3, user.getRole());
            ps.setObject(4, user.getEmpId() > 0 ? user.getEmpId() : null);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) user.setUserId(keys.getInt(1));
                System.out.println("[DAO] User created: " + user.getUsername());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] createUser error: " + e.getMessage());
        }
        return false;
    }

    public boolean changePassword(int userId, String newPlainPassword) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(CHANGE_PASSWORD)) {
            ps.setString(1, PasswordUtil.hashPassword(newPlainPassword));
            ps.setInt(2, userId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) System.out.println("[DAO] Password changed for userId=" + userId);
            return ok;
        } catch (SQLException e) {
            System.err.println("[DAO] changePassword error: " + e.getMessage());
        }
        return false;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) user.setLastLogin(lastLogin.toLocalDateTime());
        return user;
    }
}
