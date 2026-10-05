package com.teamsync.comment;

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
@RequestMapping("/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(Authentication authentication,
                                                  @PathVariable String workspaceId,
                                                  @PathVariable String projectId,
                                                  @PathVariable String taskId,
                                                  @Valid @RequestBody CreateCommentRequest request) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(userId, workspaceId, projectId, taskId, request));
    }

    @GetMapping
    public List<CommentResponse> list(Authentication authentication,
                                      @PathVariable String workspaceId,
                                      @PathVariable String projectId,
                                      @PathVariable String taskId) {
        String userId = (String) authentication.getPrincipal();
        return commentService.list(userId, workspaceId, projectId, taskId);
    }

    @PatchMapping("/{commentId}")
    public CommentResponse update(Authentication authentication,
                                  @PathVariable String workspaceId,
                                  @PathVariable String projectId,
                                  @PathVariable String taskId,
                                  @PathVariable String commentId,
                                  @Valid @RequestBody UpdateCommentRequest request) {
        String userId = (String) authentication.getPrincipal();
        return commentService.update(userId, workspaceId, projectId, taskId, commentId, request);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(Authentication authentication,
                                       @PathVariable String workspaceId,
                                       @PathVariable String projectId,
                                       @PathVariable String taskId,
                                       @PathVariable String commentId) {
        String userId = (String) authentication.getPrincipal();
        commentService.delete(userId, workspaceId, projectId, taskId, commentId);
        return ResponseEntity.noContent().build();
    }
}