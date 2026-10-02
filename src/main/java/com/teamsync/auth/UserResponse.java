package com.teamsync.auth;

import com.teamsync.user.User;

import java.time.Instant;

public record UserResponse(String id, String name, String email, Instant createdAt) {

    public UserResponse(User user) {
        this(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}