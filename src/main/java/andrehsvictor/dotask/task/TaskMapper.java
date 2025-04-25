package andrehsvictor.dotask.task;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import andrehsvictor.dotask.task.dto.GetTaskDto;
import andrehsvictor.dotask.task.dto.PostTaskDto;
import andrehsvictor.dotask.task.dto.PutTaskDto;

@Mapper(componentModel = "spring", imports = { TaskPriority.class, TaskStatus.class }, uses = { TaskPriority.class,
        TaskStatus.class })
public interface TaskMapper {

    GetTaskDto taskToGetTaskDto(Task task);

    @Mapping(target = "priority", expression = "java(TaskPriority.fromString(postTaskDto.getPriority()))")
    @Mapping(target = "status", expression = "java(TaskStatus.fromString(postTaskDto.getStatus()))")
    Task postTaskDtoToTask(PostTaskDto postTaskDto);

    @Mapping(target = "priority", expression = "java(TaskPriority.fromString(putTaskDto.getPriority()))")
    @Mapping(target = "status", expression = "java(TaskStatus.fromString(putTaskDto.getStatus()))")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Task updateTaskFromPutTaskDto(
            @MappingTarget Task task,
            PutTaskDto putTaskDto);

    default void afterMapping(
            @MappingTarget Task task,
            PutTaskDto putTaskDto) {
        if (putTaskDto.getDescription() != null && putTaskDto.getDescription().isBlank()) {
            task.setDescription(null);
        }
        if (putTaskDto.getDueDate() != null && putTaskDto.getDueDate().isBlank()) {
            task.setDueDate(null);
        }
    }

}
