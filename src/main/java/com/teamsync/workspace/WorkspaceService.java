package com.teamsync.workspace;

import com.teamsync.user.User;
import com.teamsync.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            WorkspaceMemberRepository memberRepository,
                            UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    public WorkspaceResponse create(String userId, CreateWorkspaceRequest request) {
        Workspace workspace = new Workspace();
        workspace.setName(request.name().trim());
        workspace.setDescription(request.description());
        workspace.setOwnerId(userId);
        workspace = workspaceRepository.save(workspace);

        WorkspaceMember owner = new WorkspaceMember();
        owner.setWorkspaceId(workspace.getId());
        owner.setUserId(userId);
        owner.setRole(WorkspaceRole.OWNER);
        memberRepository.save(owner);

        return new WorkspaceResponse(workspace);
    }

    public List<WorkspaceSummary> listForUser(String userId) {
        Map<String, WorkspaceRole> roles = memberRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(WorkspaceMember::getWorkspaceId, WorkspaceMember::getRole));

        return workspaceRepository.findAllById(roles.keySet()).stream()
                .map(w -> new WorkspaceSummary(w.getId(), w.getName(), w.getDescription(),
                        roles.get(w.getId()), w.getCreatedAt()))
                .toList();
    }

    public WorkspaceSummary getForMember(String userId, String workspaceId) {
        WorkspaceMember member = memberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
        Workspace w = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
        return new WorkspaceSummary(w.getId(), w.getName(), w.getDescription(),
                member.getRole(), w.getCreatedAt());
    }

    public MemberResponse addMember(String callerId, String workspaceId, AddMemberRequest request) {
        WorkspaceMember caller = memberRepository.findByWorkspaceIdAndUserId(workspaceId, callerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        if (caller.getRole() != WorkspaceRole.OWNER && caller.getRole() != WorkspaceRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owners and admins can add members");
        }
        if (request.role() == WorkspaceRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign the OWNER role");
        }

        User user = userRepository.findByEmail(request.email().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No user with that email"));

        if (memberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member");
        }

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspaceId);
        member.setUserId(user.getId());
        member.setRole(request.role());
        member = memberRepository.save(member);

        return new MemberResponse(user.getId(), user.getName(), user.getEmail(),
                member.getRole(), member.getJoinedAt());
    }

    public List<MemberResponse> listMembers(String callerId, String workspaceId) {
        memberRepository.findByWorkspaceIdAndUserId(workspaceId, callerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        List<WorkspaceMember> members = memberRepository.findByWorkspaceId(workspaceId);
        Map<String, User> users = userRepository
                .findAllById(members.stream().map(WorkspaceMember::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return members.stream()
                .map(m -> {
                    User u = users.get(m.getUserId());
                    return new MemberResponse(m.getUserId(),
                            u == null ? null : u.getName(),
                            u == null ? null : u.getEmail(),
                            m.getRole(), m.getJoinedAt());
                })
                .toList();
    }

    public void removeMember(String callerId, String workspaceId, String targetUserId) {
        WorkspaceMember caller = memberRepository.findByWorkspaceIdAndUserId(workspaceId, callerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
        WorkspaceMember target = memberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        boolean self = callerId.equals(targetUserId);
        boolean canManage = caller.getRole() == WorkspaceRole.OWNER
                || caller.getRole() == WorkspaceRole.ADMIN;

        if (!self && !canManage) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owners and admins can remove members");
        }
        if (target.getRole() == WorkspaceRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The owner cannot be removed");
        }
        if (!self && caller.getRole() == WorkspaceRole.ADMIN && target.getRole() == WorkspaceRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins cannot remove other admins");
        }

        memberRepository.delete(target);
    }

    public MemberResponse changeRole(String callerId, String workspaceId, String targetUserId,
                                     UpdateMemberRoleRequest request) {
        WorkspaceMember caller = memberRepository.findByWorkspaceIdAndUserId(workspaceId, callerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        if (caller.getRole() != WorkspaceRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can change roles");
        }
        if (request.role() == WorkspaceRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign the OWNER role");
        }

        WorkspaceMember target = memberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        if (target.getRole() == WorkspaceRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The owner's role cannot be changed");
        }

        target.setRole(request.role());
        target = memberRepository.save(target);

        User u = userRepository.findById(targetUserId).orElse(null);
        return new MemberResponse(targetUserId,
                u == null ? null : u.getName(),
                u == null ? null : u.getEmail(),
                target.getRole(), target.getJoinedAt());
    }
}