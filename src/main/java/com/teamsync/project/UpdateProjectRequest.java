package com.teamsync.project;

import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @Size(max = 100) String name,
        @Size(max = 500) String description
) {
}