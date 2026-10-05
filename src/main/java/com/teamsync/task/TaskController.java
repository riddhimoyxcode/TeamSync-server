package com.teamsync.task;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(Authentication authentication,
                                               @PathVariable String workspaceId,
                                               @PathVariable String projectId,
                                               @Valid @RequestBody CreateTaskRequest request) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(userId, workspaceId, projectId, request));
    }

    @GetMapping
    public List<TaskResponse> list(Authentication authentication,
                                   @PathVariable String workspaceId,
                                   @PathVariable String projectId,
                                   @RequestParam(required = false) TaskStatus status) {
        String userId = (String) authentication.getPrincipal();
        return taskService.list(userId, workspaceId, projectId, status);
    }

    @GetMapping("/{taskId}")
    public TaskResponse get(Authentication authentication,
                            @PathVariable String workspaceId,
                            @PathVariable String projectId,
                            @PathVariable String taskId) {
        String userId = (String) authentication.getPrincipal();
        return taskService.get(userId, workspaceId, projectId, taskId);
    }

    @PatchMapping("/{taskId}")
    public TaskResponse update(Authentication authentication,
                               @PathVariable String workspaceId,
                               @PathVariable String projectId,
                               @PathVariable String taskId,
                               @Valid @RequestBody UpdateTaskRequest request) {
        String userId = (String) authentication.getPrincipal();
        return taskService.update(userId, workspaceId, projectId, taskId, request);
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse updateStatus(Authentication authentication,
                                     @PathVariable String workspaceId,
                                     @PathVariable String projectId,
                                     @PathVariable String taskId,
                                     @Valid @RequestBody UpdateTaskStatusRequest request) {
        String userId = (String) authentication.getPrincipal();
        return taskService.updateStatus(userId, workspaceId, projectId, taskId, request);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(Authentication authentication,
                                       @PathVariable String workspaceId,
                                       @PathVariable String projectId,
                                       @PathVariable String taskId) {
        String userId = (String) authentication.getPrincipal();
        taskService.delete(userId, workspaceId, projectId, taskId);
        return ResponseEntity.noContent().build();
    }
}