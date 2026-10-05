package com.teamsync.task;

import com.teamsync.project.ProjectRepository;
import com.teamsync.workspace.WorkspaceMember;
import com.teamsync.workspace.WorkspaceMemberRepository;
import com.teamsync.workspace.WorkspaceRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceMemberRepository memberRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       WorkspaceMemberRepository memberRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
    }

    public TaskResponse create(String callerId, String workspaceId, String projectId,
                               CreateTaskRequest request) {
        requireMember(callerId, workspaceId);
        requireProject(workspaceId, projectId);

        String assigneeId = null;
        if (request.assigneeId() != null && !request.assigneeId().isBlank()) {
            requireAssignee(workspaceId, request.assigneeId());
            assigneeId = request.assigneeId();
        }

        Task task = new Task();
        task.setWorkspaceId(workspaceId);
        task.setProjectId(projectId);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setAssigneeId(assigneeId);
        task.setStatus(TaskStatus.TODO);
        task.setCreatedBy(callerId);
        task = taskRepository.save(task);

        return new TaskResponse(task);
    }

    public List<TaskResponse> list(String callerId, String workspaceId, String projectId,
                                   TaskStatus status) {
        requireMember(callerId, workspaceId);
        requireProject(workspaceId, projectId);

        List<Task> tasks = (status == null)
                ? taskRepository.findByProjectId(projectId)
                : taskRepository.findByProjectIdAndStatus(projectId, status);

        return tasks.stream().map(TaskResponse::new).toList();
    }

    public TaskResponse get(String callerId, String workspaceId, String projectId, String taskId) {
        requireMember(callerId, workspaceId);
        requireProject(workspaceId, projectId);
        return new TaskResponse(findTask(projectId, taskId));
    }

    public TaskResponse updateStatus(String callerId, String workspaceId, String projectId,
                                     String taskId, UpdateTaskStatusRequest request) {
        requireMember(callerId, workspaceId);
        requireProject(workspaceId, projectId);

        Task task = findTask(projectId, taskId);
        task.setStatus(request.status());
        return new TaskResponse(taskRepository.save(task));
    }

    public TaskResponse update(String callerId, String workspaceId, String projectId,
                               String taskId, UpdateTaskRequest request) {
        WorkspaceMember caller = requireMember(callerId, workspaceId);
        requireProject(workspaceId, projectId);
        Task task = findTask(projectId, taskId);

        boolean canEdit = caller.getRole() != WorkspaceRole.MEMBER
                || callerId.equals(task.getCreatedBy())
                || callerId.equals(task.getAssigneeId());
        if (!canEdit) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot edit this task");
        }

        if (request.title() != null) {
            String title = request.title().trim();
            if (title.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be blank");
            }
            task.setTitle(title);
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.assigneeId() != null) {
            if (request.assigneeId().isBlank()) {
                task.setAssigneeId(null);
            } else {
                requireAssignee(workspaceId, request.assigneeId());
                task.setAssigneeId(request.assigneeId());
            }
        }

        return new TaskResponse(taskRepository.save(task));
    }

    public void delete(String callerId, String workspaceId, String projectId, String taskId) {
        WorkspaceMember caller = requireMember(callerId, workspaceId);
        requireProject(workspaceId, projectId);
        Task task = findTask(projectId, taskId);

        boolean canDelete = caller.getRole() != WorkspaceRole.MEMBER
                || callerId.equals(task.getCreatedBy());
        if (!canDelete) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete this task");
        }

        taskRepository.delete(task);
    }

    private WorkspaceMember requireMember(String callerId, String workspaceId) {
        return memberRepository.findByWorkspaceIdAndUserId(workspaceId, callerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
    }

    private void requireProject(String workspaceId, String projectId) {
        projectRepository.findById(projectId)
                .filter(p -> p.getWorkspaceId().equals(workspaceId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private Task findTask(String projectId, String taskId) {
        return taskRepository.findById(taskId)
                .filter(t -> t.getProjectId().equals(projectId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private void requireAssignee(String workspaceId, String assigneeId) {
        memberRepository.findByWorkspaceIdAndUserId(workspaceId, assigneeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Assignee must be a member of this workspace"));
    }
}