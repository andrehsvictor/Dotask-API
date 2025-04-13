package andrehsvictor.dotask.project;

import java.util.Collection;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import andrehsvictor.dotask.exception.ResourceNotFoundException;
import andrehsvictor.dotask.jwt.JwtService;
import andrehsvictor.dotask.project.dto.GetProjectDto;
import andrehsvictor.dotask.project.dto.PostProjectDto;
import andrehsvictor.dotask.project.dto.PutProjectDto;
import andrehsvictor.dotask.user.User;
import andrehsvictor.dotask.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final JwtService jwtService;
    private final ProjectMapper projectMapper;
    private final UserService userService;

    public GetProjectDto toDto(Project project) {
        return projectMapper.projectToGetProjectDto(project);
    }

    public boolean existsById(UUID id) {
        UUID userId = jwtService.getCurrentUserUuid();
        return projectRepository.existsByIdAndUserId(id, userId);
    }

    public Page<Project> findAll(String query, Pageable pageable) {
        UUID userId = jwtService.getCurrentUserUuid();
        return projectRepository.findAllByUserIdWithFilter(userId, query, pageable);
    }
    
    public Project findById(UUID id) {
        UUID userId = jwtService.getCurrentUserUuid();
        return projectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(Project.class, "ID", id));
    }

    public Project create(PostProjectDto postProjectDto) {
        Project project = projectMapper.postProjectDtoToProject(postProjectDto);
        User user = userService.findMe();
        project.setUser(user);
        return projectRepository.save(project);
    }

    public Project update(UUID id, PutProjectDto putProjectDto) {
        Project project = findById(id);
        projectMapper.updateProjectFromPutProjectDto(project, putProjectDto);
        return projectRepository.save(project);
    }

    public void delete(UUID id) {
        UUID userId = jwtService.getCurrentUserUuid();
        if (projectRepository.deleteByIdAndUserId(id, userId) == 0) {
            throw new ResourceNotFoundException(Project.class, "ID", id);
        }
    }

    public void deleteAllByIdIn(Collection<UUID> ids) {
        UUID userId = jwtService.getCurrentUserUuid();
        projectRepository.deleteAllByUserIdAndIdIn(userId, ids);
    }

    public Project incrementTaskCount(Project project) {
        project.setTaskCount(project.getTaskCount() + 1);
        return projectRepository.save(project);
    }

    public Project decrementTaskCount(Project project) {
        project.setTaskCount(project.getTaskCount() - 1);
        return projectRepository.save(project);
    }

}
