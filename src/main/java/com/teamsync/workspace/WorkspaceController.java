package com.teamsync.workspace;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(Authentication authentication,
                                                    @Valid @RequestBody CreateWorkspaceRequest request) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.create(userId, request));
    }

    @GetMapping
    public List<WorkspaceSummary> list(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return workspaceService.listForUser(userId);
    }

    @GetMapping("/{id}")
    public WorkspaceSummary get(Authentication authentication, @PathVariable String id) {
        String userId = (String) authentication.getPrincipal();
        return workspaceService.getForMember(userId, id);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<MemberResponse> addMember(Authentication authentication,
                                                    @PathVariable String id,
                                                    @Valid @RequestBody AddMemberRequest request) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.addMember(userId, id, request));
    }

    @GetMapping("/{id}/members")
    public List<MemberResponse> listMembers(Authentication authentication,
                                            @PathVariable String id) {
        String userId = (String) authentication.getPrincipal();
        return workspaceService.listMembers(userId, id);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(Authentication authentication,
                                             @PathVariable String id,
                                             @PathVariable String userId) {
        String callerId = (String) authentication.getPrincipal();
        workspaceService.removeMember(callerId, id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/members/{userId}")
    public MemberResponse changeRole(Authentication authentication,
                                     @PathVariable String id,
                                     @PathVariable String userId,
                                     @Valid @RequestBody UpdateMemberRoleRequest request) {
        String callerId = (String) authentication.getPrincipal();
        return workspaceService.changeRole(callerId, id, userId, request);
    }
}