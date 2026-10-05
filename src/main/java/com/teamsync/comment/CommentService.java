package com.teamsync.comment;

import com.teamsync.task.TaskRepository;
import com.teamsync.user.User;
import com.teamsync.user.UserRepository;
import com.teamsync.workspace.WorkspaceMember;
import com.teamsync.workspace.WorkspaceMemberRepository;
import com.teamsync.workspace.WorkspaceRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          TaskRepository taskRepository,
                          WorkspaceMemberRepository memberRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    public CommentResponse create(String callerId, String workspaceId, String projectId,
                                  String taskId, CreateCommentRequest request) {
        requireMember(callerId, workspaceId);
        requireTask(workspaceId, projectId, taskId);

        Comment comment = new Comment();
        comment.setWorkspaceId(workspaceId);
        comment.setTaskId(taskId);
        comment.setAuthorId(callerId);
        comment.setText(request.text().trim());
        comment = commentRepository.save(comment);

        return new CommentResponse(comment, nameOf(callerId));
    }

    public List<CommentResponse> list(String callerId, String workspaceId, String projectId,
                                      String taskId) {
        requireMember(callerId, workspaceId);
        requireTask(workspaceId, projectId, taskId);

        List<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);

        List<String> authorIds = comments.stream().map(Comment::getAuthorId).distinct().toList();
        Map<String, String> names = new HashMap<>();
        for (User u : userRepository.findAllById(authorIds)) {
            names.put(u.getId(), u.getName());
        }

        return comments.stream()
                .map(c -> new CommentResponse(c, names.get(c.getAuthorId())))
                .toList();
    }

    public CommentResponse update(String callerId, String workspaceId, String projectId,
                                  String taskId, String commentId, UpdateCommentRequest request) {
        requireMember(callerId, workspaceId);
        requireTask(workspaceId, projectId, taskId);
        Comment comment = findComment(taskId, commentId);

        if (!callerId.equals(comment.getAuthorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can edit a comment");
        }

        comment.setText(request.text().trim());
        comment = commentRepository.save(comment);
        return new CommentResponse(comment, nameOf(callerId));
    }

    public void delete(String callerId, String workspaceId, String projectId,
                       String taskId, String commentId) {
        WorkspaceMember caller = requireMember(callerId, workspaceId);
        requireTask(workspaceId, projectId, taskId);
        Comment comment = findComment(taskId, commentId);

        boolean canDelete = callerId.equals(comment.getAuthorId())
                || caller.getRole() != WorkspaceRole.MEMBER;
        if (!canDelete) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete this comment");
        }

        commentRepository.delete(comment);
    }

    private String nameOf(String userId) {
        return userRepository.findById(userId).map(User::getName).orElse(null);
    }

    private WorkspaceMember requireMember(String callerId, String workspaceId) {
        return memberRepository.findByWorkspaceIdAndUserId(workspaceId, callerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
    }

    private void requireTask(String workspaceId, String projectId, String taskId) {
        taskRepository.findById(taskId)
                .filter(t -> t.getWorkspaceId().equals(workspaceId) && t.getProjectId().equals(projectId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private Comment findComment(String taskId, String commentId) {
        return commentRepository.findById(commentId)
                .filter(c -> c.getTaskId().equals(taskId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }
}