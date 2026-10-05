package com.teamsync.task;

import java.time.Instant;

public record TaskResponse(String id, String workspaceId, String projectId, String title,
                           String description, TaskStatus status, String assigneeId,
                           String createdBy, Instant createdAt) {

    public TaskResponse(Task t) {
        this(t.getId(), t.getWorkspaceId(), t.getProjectId(), t.getTitle(), t.getDescription(),
                t.getStatus(), t.getAssigneeId(), t.getCreatedBy(), t.getCreatedAt());
    }
}