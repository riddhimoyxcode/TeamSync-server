package com.teamsync.workspace;

import java.time.Instant;

public record WorkspaceSummary(String id, String name, String description, WorkspaceRole role, Instant createdAt){

}