package com.duesguard.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class AppUser {
    private String id;
    private String username;
    private String email;
    private String passwordHash;
    private boolean onboardingCompleted;
    private boolean demoMode;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public AppUser() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.onboardingCompleted = false;
        this.demoMode = false;
    }

    public AppUser(String username, String email, String passwordHash) {
        this();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isOnboardingCompleted() { return onboardingCompleted; }
    public void setOnboardingCompleted(boolean onboardingCompleted) { this.onboardingCompleted = onboardingCompleted; }
    public boolean isDemoMode() { return demoMode; }
    public void setDemoMode(boolean demoMode) { this.demoMode = demoMode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
