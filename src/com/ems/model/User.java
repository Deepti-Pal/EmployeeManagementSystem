package com.ems.model;

import java.time.LocalDateTime;

/**
 * User - Represents a system login account (maps to `users` table).
 */
public class User {

    private int    userId;
    private String username;
    private String passwordHash;
    private String role;          // ADMIN | HR | VIEWER
    private int    empId;
    private boolean isActive;
    private LocalDateTime lastLogin;

    public User() {}

    public User(String username, String passwordHash, String role) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.isActive     = true;
    }

    // ── Role helpers ─────────────────────────────────────────────────────────

    public boolean isAdmin()  { return "ADMIN".equalsIgnoreCase(role); }
    public boolean isHR()     { return "HR".equalsIgnoreCase(role); }
    public boolean isViewer() { return "VIEWER".equalsIgnoreCase(role); }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int    getUserId()         { return userId; }
    public void   setUserId(int v)    { this.userId = v; }

    public String getUsername()       { return username; }
    public void   setUsername(String v){ this.username = v; }

    public String getPasswordHash()   { return passwordHash; }
    public void   setPasswordHash(String v) { this.passwordHash = v; }

    public String getRole()           { return role; }
    public void   setRole(String v)   { this.role = v; }

    public int    getEmpId()          { return empId; }
    public void   setEmpId(int v)     { this.empId = v; }

    public boolean isActive()         { return isActive; }
    public void    setActive(boolean v){ this.isActive = v; }

    public LocalDateTime getLastLogin()          { return lastLogin; }
    public void          setLastLogin(LocalDateTime v) { this.lastLogin = v; }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role='%s', active=%b}",
            userId, username, role, isActive);
    }
}
