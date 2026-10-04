package com.teamsync.project;

import java.time.Instant;

public record ProjectResponse(String id, String workspaceId, String name, String description,
                              String createdBy, Instant createdAt) {

    public ProjectResponse(Project p) {
        this(p.getId(), p.getWorkspaceId(), p.getName(), p.getDescription(),
                p.getCreatedBy(), p.getCreatedAt());
    }
}