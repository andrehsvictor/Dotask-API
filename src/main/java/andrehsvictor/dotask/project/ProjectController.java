package andrehsvictor.dotask.project;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andrehsvictor.dotask.project.dto.GetProjectDto;
import andrehsvictor.dotask.project.dto.PostProjectDto;
import andrehsvictor.dotask.project.dto.PutProjectDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/api/v1/projects")
    public ResponseEntity<Page<GetProjectDto>> findAll(
            @RequestParam(value = "q") String query,
            Pageable pageable) {
        Page<GetProjectDto> projects = projectService.findAll(query, pageable)
                .map(projectService::toDto);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/api/v1/projects/{id}")
    public ResponseEntity<GetProjectDto> findById(@PathVariable UUID id) {
        GetProjectDto project = projectService.toDto(projectService.findById(id));
        return ResponseEntity.ok(project);
    }

    @PostMapping("/api/v1/projects")
    public ResponseEntity<GetProjectDto> create(@Valid @RequestBody PostProjectDto postProjectDto) {
        GetProjectDto project = projectService.toDto(projectService.create(postProjectDto));
        return ResponseEntity.status(201).body(project);
    }

    @PutMapping("/api/v1/projects/{id}")
    public ResponseEntity<GetProjectDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody PutProjectDto putProjectDto) {
        GetProjectDto project = projectService.toDto(projectService.update(id, putProjectDto));
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/api/v1/projects/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v1/projects")
    public ResponseEntity<Void> deleteAllByIdIn(@RequestParam UUID[] ids) {
        projectService.deleteAllByIdIn(List.of(ids));
        return ResponseEntity.noContent().build();
    }

}
