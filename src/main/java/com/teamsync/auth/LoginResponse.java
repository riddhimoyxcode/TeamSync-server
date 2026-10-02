package com.teamsync.auth;

public record LoginResponse(String token, UserResponse user) {
}