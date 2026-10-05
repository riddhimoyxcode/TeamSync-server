package com.teamsync.task;

import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @Size(max = 200) String title,
        @Size(max = 2000) String description,
        String assigneeId
) {
}