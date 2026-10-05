package com.teamsync.workspace;

import com.teamsync.user.User;
import com.teamsync.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    private WorkspaceMember memberWithRole(WorkspaceRole role) {
        WorkspaceMember m = new WorkspaceMember();
        m.setRole(role);
        return m;
    }

    private User userWithId(String id) {
        User u = new User();
        u.setId(id);
        u.setName("New Person");
        u.setEmail("new@example.com");
        return u;
    }

    @Test
    void plainMemberCannotAddMembers() {
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "caller"))
                .thenReturn(Optional.of(memberWithRole(WorkspaceRole.MEMBER)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> workspaceService.addMember("caller", "w1",
                        new AddMemberRequest("new@example.com", WorkspaceRole.MEMBER)));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void nobodyCanBeAssignedOwnerRole() {
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "caller"))
                .thenReturn(Optional.of(memberWithRole(WorkspaceRole.OWNER)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> workspaceService.addMember("caller", "w1",
                        new AddMemberRequest("new@example.com", WorkspaceRole.OWNER)));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void addingSameUserTwiceIsRejected() {
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "caller"))
                .thenReturn(Optional.of(memberWithRole(WorkspaceRole.ADMIN)));
        when(userRepository.findByEmail("new@example.com"))
                .thenReturn(Optional.of(userWithId("u2")));
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "u2"))
                .thenReturn(Optional.of(memberWithRole(WorkspaceRole.MEMBER)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> workspaceService.addMember("caller", "w1",
                        new AddMemberRequest("new@example.com", WorkspaceRole.MEMBER)));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void ownerCanAddMember() {
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "caller"))
                .thenReturn(Optional.of(memberWithRole(WorkspaceRole.OWNER)));
        when(userRepository.findByEmail("new@example.com"))
                .thenReturn(Optional.of(userWithId("u2")));
        when(memberRepository.findByWorkspaceIdAndUserId("w1", "u2"))
                .thenReturn(Optional.empty());
        when(memberRepository.save(any(WorkspaceMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MemberResponse response = workspaceService.addMember("caller", "w1",
                new AddMemberRequest("new@example.com", WorkspaceRole.MEMBER));

        assertEquals(WorkspaceRole.MEMBER, response.role());
        verify(memberRepository).save(any(WorkspaceMember.class));
    }
}