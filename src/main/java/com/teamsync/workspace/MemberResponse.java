package com.teamsync.workspace;

import java.time.Instant;

public record MemberResponse(String userId, String name, String email,
                             WorkspaceRole role, Instant joinedAt) {
}