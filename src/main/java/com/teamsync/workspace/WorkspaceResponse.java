package com.teamsync.workspace;

import java.time.Instant;

public record WorkspaceResponse(String id, String name, String description, String ownerId, Instant createdAt){

    public WorkspaceResponse(Workspace w){
        this(w.getId(), w.getName(), w.getDescription(), w.getOwnerId(), w.getCreatedAt());
    }
}