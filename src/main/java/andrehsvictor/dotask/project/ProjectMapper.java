package andrehsvictor.dotask.project;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import andrehsvictor.dotask.project.dto.GetProjectDto;
import andrehsvictor.dotask.project.dto.PostProjectDto;
import andrehsvictor.dotask.project.dto.PutProjectDto;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    GetProjectDto projectToGetProjectDto(Project project);

    Project postProjectDtoToProject(PostProjectDto postProjectDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Project updateProjectFromPutProjectDto(
            @MappingTarget Project project,
            PutProjectDto putProjectDto);

    default void afterMapping(
            @MappingTarget Project project,
            PutProjectDto putProjectDto) {
        if (putProjectDto.getDescription() != null && putProjectDto.getDescription().isBlank()) {
            project.setDescription(null);
        }
    }
}