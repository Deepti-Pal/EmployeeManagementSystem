package com.ems.auth;

import com.ems.dao.UserDAO;
import com.ems.model.User;

/**
 * AuthService - Manages the current login session and role-based access control.
 * Singleton: only one session active at a time.
 */
public class AuthService {

    private static AuthService instance;
    private static User currentUser = null;
    private final UserDAO userDAO = new UserDAO();

    private AuthService() {}

    public static AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    // ── Login / Logout ───────────────────────────────────────────────────────

    /**
     * Attempts login with the given credentials.
     * @return true if authentication succeeded
     */
    public boolean login(String username, String password) {
        User user = userDAO.authenticate(username, password);
        if (user != null) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        System.out.println("[Auth] Logging out: " + (currentUser != null ? currentUser.getUsername() : "nobody"));
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // ── Session info ─────────────────────────────────────────────────────────

    public User getCurrentUser() { return currentUser; }

    public String getCurrentRole() {
        return isLoggedIn() ? currentUser.getRole() : "NONE";
    }

    // ── Permission checks ────────────────────────────────────────────────────

    /** Admins can do everything. */
    public boolean isAdmin() {
        return isLoggedIn() && currentUser.isAdmin();
    }

    /** HR and Admins can modify employee/salary data. */
    public boolean canWrite() {
        return isLoggedIn() && (currentUser.isAdmin() || currentUser.isHR());
    }

    /** Everyone logged in can view. */
    public boolean canRead() {
        return isLoggedIn();
    }

    /**
     * Throws a RuntimeException if the user does not have write permission.
     * Call this at the top of any mutating service method.
     */
    public void requireWriteAccess() {
        if (!canWrite()) {
            throw new SecurityException("Access denied: ADMIN or HR role required for this operation.");
        }
    }

    public void requireAdminAccess() {
        if (!isAdmin()) {
            throw new SecurityException("Access denied: ADMIN role required.");
        }
    }
}
