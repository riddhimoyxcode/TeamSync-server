package com.teamsync.project;

import com.teamsync.comment.CommentRepository;
import com.teamsync.task.Task;
import com.teamsync.task.TaskRepository;
import com.teamsync.workspace.WorkspaceMember;
import com.teamsync.workspace.WorkspaceMemberRepository;
import com.teamsync.workspace.WorkspaceRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkspaceMemberRepository memberRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ProjectService projectService;

    private WorkspaceMember member(WorkspaceRole role) {
        WorkspaceMember m = new WorkspaceMember();
        m.setRole(role);
        return m;
    }

    @Test
    void deletingProjectAlsoDeletesItsTasksAndComments() {
        Project project = new Project();
        project.setWorkspaceId("w1");
        Task t1 = new Task();
        t1.setId("t1");
        Task t2 = new Task();
        t2.setId("t2");
        List<Task> tasks = List.of(t1, t2);

        when(memberRepository.findByWorkspaceIdAndUserId("w1", "caller"))
                .thenReturn(Optional.of(member(WorkspaceRole.OWNER)));
        when(projectRepository.findById("p1")).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId("p1")).thenReturn(tasks);

        projectService.delete("caller", "w1", "p1");

        verify(commentRepository).deleteByTaskId("t1");
        verify(commentRepository).deleteByTaskId("t2");
        verify(taskRepository).deleteAll(tasks);
        verify(projectRepository).delete(project);
    }

    @Test
    void plainMemberCannotDeleteProject() {
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "caller"))
                .thenReturn(Optional.of(member(WorkspaceRole.MEMBER)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> projectService.delete("caller", "w1", "p1"));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void managerCannotDeleteProject() {
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "caller"))
                .thenReturn(Optional.of(member(WorkspaceRole.MANAGER)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> projectService.delete("caller", "w1", "p1"));

        assertEquals(403, ex.getStatusCode().value());
    }
}