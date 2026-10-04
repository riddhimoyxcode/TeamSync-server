package com.teamsync.project;

import com.teamsync.workspace.WorkspaceMember;
import com.teamsync.workspace.WorkspaceMemberRepository;
import com.teamsync.workspace.WorkspaceRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceMemberRepository memberRepository;

    public ProjectService(ProjectRepository projectRepository,
                          WorkspaceMemberRepository memberRepository) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
    }

    public ProjectResponse create(String callerId, String workspaceId, CreateProjectRequest request) {
        WorkspaceMember caller = requireMember(callerId, workspaceId);

        if (caller.getRole() == WorkspaceRole.MEMBER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Members cannot create projects");
        }

        Project project = new Project();
        project.setWorkspaceId(workspaceId);
        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setCreatedBy(callerId);
        project = projectRepository.save(project);

        return new ProjectResponse(project);
    }

    public List<ProjectResponse> list(String callerId, String workspaceId) {
        requireMember(callerId, workspaceId);
        return projectRepository.findByWorkspaceId(workspaceId).stream()
                .map(ProjectResponse::new)
                .toList();
    }

    public ProjectResponse get(String callerId, String workspaceId, String projectId) {
        requireMember(callerId, workspaceId);
        return new ProjectResponse(findProject(workspaceId, projectId));
    }

    public ProjectResponse update(String callerId, String workspaceId, String projectId,
                                  UpdateProjectRequest request) {
        WorkspaceMember caller = requireMember(callerId, workspaceId);

        if (caller.getRole() == WorkspaceRole.MEMBER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Members cannot edit projects");
        }

        Project project = findProject(workspaceId, projectId);

        if (request.name() != null) {
            String name = request.name().trim();
            if (name.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
            }
            project.setName(name);
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }

        return new ProjectResponse(projectRepository.save(project));
    }

    public void delete(String callerId, String workspaceId, String projectId) {
        WorkspaceMember caller = requireMember(callerId, workspaceId);

        if (caller.getRole() != WorkspaceRole.OWNER && caller.getRole() != WorkspaceRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owners and admins can delete projects");
        }

        projectRepository.delete(findProject(workspaceId, projectId));
    }

    private WorkspaceMember requireMember(String callerId, String workspaceId) {
        return memberRepository.findByWorkspaceIdAndUserId(workspaceId, callerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
    }

    private Project findProject(String workspaceId, String projectId) {
        return projectRepository.findById(projectId)
                .filter(p -> p.getWorkspaceId().equals(workspaceId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }
}