package com.teamsync.task;

import com.teamsync.comment.CommentRepository;
import com.teamsync.project.Project;
import com.teamsync.project.ProjectRepository;
import com.teamsync.workspace.WorkspaceMember;
import com.teamsync.workspace.WorkspaceMemberRepository;
import com.teamsync.workspace.WorkspaceRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkspaceMemberRepository memberRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private TaskService taskService;

    private WorkspaceMember member(WorkspaceRole role) {
        WorkspaceMember m = new WorkspaceMember();
        m.setRole(role);
        return m;
    }

    private Project project() {
        Project p = new Project();
        p.setWorkspaceId("w1");
        return p;
    }

    private Task someoneElsesTask() {
        Task t = new Task();
        t.setProjectId("p1");
        t.setCreatedBy("other");
        t.setAssigneeId("another");
        return t;
    }

    @Test
    void nonMemberGetsNotFoundWhenCreatingTask() {
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "caller"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.create("caller", "w1", "p1",
                        new CreateTaskRequest("Title", null, null)));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void assigneeMustBeWorkspaceMember() {
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "caller"))
                .thenReturn(Optional.of(member(WorkspaceRole.MEMBER)));
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project()));
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "stranger"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.create("caller", "w1", "p1",
                        new CreateTaskRequest("Title", null, "stranger")));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void plainMemberCannotEditSomeoneElsesTask() {
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "caller"))
                .thenReturn(Optional.of(member(WorkspaceRole.MEMBER)));
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project()));
        when(taskRepository.findById("t1")).thenReturn(Optional.of(someoneElsesTask()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.update("caller", "w1", "p1", "t1",
                        new UpdateTaskRequest("New title", null, null)));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void plainMemberCannotDeleteSomeoneElsesTask() {
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "caller"))
                .thenReturn(Optional.of(member(WorkspaceRole.MEMBER)));
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project()));
        when(taskRepository.findById("t1")).thenReturn(Optional.of(someoneElsesTask()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.delete("caller", "w1", "p1", "t1"));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void managerCanDeleteAnyTaskAndItsComments() {
        Task task = someoneElsesTask();
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "caller"))
                .thenReturn(Optional.of(member(WorkspaceRole.MANAGER)));
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project()));
        when(taskRepository.findById("t1")).thenReturn(Optional.of(task));

        taskService.delete("caller", "w1", "p1", "t1");

        verify(commentRepository).deleteByTaskId("t1");
        verify(taskRepository).delete(task);
    }
}