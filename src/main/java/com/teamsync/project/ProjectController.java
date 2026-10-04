package com.teamsync.project;

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
@RequestMapping("/api/workspaces/{workspaceId}/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(Authentication authentication,
                                                  @PathVariable String workspaceId,
                                                  @Valid @RequestBody CreateProjectRequest request) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.create(userId, workspaceId, request));
    }

    @GetMapping
    public List<ProjectResponse> list(Authentication authentication,
                                      @PathVariable String workspaceId) {
        String userId = (String) authentication.getPrincipal();
        return projectService.list(userId, workspaceId);
    }

    @GetMapping("/{projectId}")
    public ProjectResponse get(Authentication authentication,
                               @PathVariable String workspaceId,
                               @PathVariable String projectId) {
        String userId = (String) authentication.getPrincipal();
        return projectService.get(userId, workspaceId, projectId);
    }

    @PatchMapping("/{projectId}")
    public ProjectResponse update(Authentication authentication,
                                  @PathVariable String workspaceId,
                                  @PathVariable String projectId,
                                  @Valid @RequestBody UpdateProjectRequest request) {
        String userId = (String) authentication.getPrincipal();
        return projectService.update(userId, workspaceId, projectId, request);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> delete(Authentication authentication,
                                       @PathVariable String workspaceId,
                                       @PathVariable String projectId) {
        String userId = (String) authentication.getPrincipal();
        projectService.delete(userId, workspaceId, projectId);
        return ResponseEntity.noContent().build();
    }
}