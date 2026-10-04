package com.teamsync.workspace;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends MongoRepository<WorkspaceMember, String> {

    List<WorkspaceMember> findByUserId(String userId);

    List<WorkspaceMember> findByWorkspaceId(String workspaceId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(String workspaceId, String userId);
}