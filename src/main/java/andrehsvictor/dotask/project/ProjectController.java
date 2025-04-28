package andrehsvictor.dotask.project;

import java.util.Collection;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Projects", description = "API for project management")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Find all projects", description = "Retrieves all projects for the authenticated user with optional filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully", content = @Content(schema = @Schema(implementation = GetProjectDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/projects")
    public ResponseEntity<Page<GetProjectDto>> findAll(
            @Parameter(description = "Search query to filter projects by name or description") @RequestParam(value = "q", required = false) String query,
            Pageable pageable) {
        Page<GetProjectDto> projects = projectService.findAll(query, pageable)
                .map(projectService::toDto);
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Find project by ID", description = "Retrieves a specific project by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project retrieved successfully", content = @Content(schema = @Schema(implementation = GetProjectDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/projects/{id}")
    public ResponseEntity<GetProjectDto> findById(
            @Parameter(description = "Project ID to retrieve") @PathVariable UUID id) {
        GetProjectDto project = projectService.toDto(projectService.findById(id));
        return ResponseEntity.ok(project);
    }

    @Operation(summary = "Create a new project", description = "Creates a new project for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created successfully", content = @Content(schema = @Schema(implementation = GetProjectDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid project data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/api/v1/projects")
    public ResponseEntity<GetProjectDto> create(
            @Parameter(description = "Project data") @Valid @RequestBody PostProjectDto postProjectDto) {
        GetProjectDto project = projectService.toDto(projectService.create(postProjectDto));
        return ResponseEntity.status(201).body(project);
    }

    @Operation(summary = "Update project", description = "Updates all fields of an existing project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated successfully", content = @Content(schema = @Schema(implementation = GetProjectDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid project data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/api/v1/projects/{id}")
    public ResponseEntity<GetProjectDto> update(
            @Parameter(description = "Project ID to update") @PathVariable UUID id,
            @Parameter(description = "Updated project data") @Valid @RequestBody PutProjectDto putProjectDto) {
        GetProjectDto project = projectService.toDto(projectService.update(id, putProjectDto));
        return ResponseEntity.ok(project);
    }

    @Operation(summary = "Delete project", description = "Deletes a project and all its associated tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/api/v1/projects/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Project ID to delete") @PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete multiple projects", description = "Deletes multiple projects by their IDs along with their associated tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Projects deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid project IDs"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/api/v1/projects")
    public ResponseEntity<Void> deleteAllByIdIn(
            @Parameter(description = "Collection of project IDs to delete") @RequestBody Collection<UUID> ids) {
        projectService.deleteAllByIdIn(ids);
        return ResponseEntity.noContent().build();
    }
}