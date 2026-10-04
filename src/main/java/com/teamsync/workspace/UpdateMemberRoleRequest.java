package com.teamsync.workspace;

import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(@NotNull WorkspaceRole role){
}