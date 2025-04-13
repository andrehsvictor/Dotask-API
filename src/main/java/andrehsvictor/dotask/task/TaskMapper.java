package andrehsvictor.dotask.task;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import andrehsvictor.dotask.task.dto.GetTaskDto;
import andrehsvictor.dotask.task.dto.PostTaskDto;
import andrehsvictor.dotask.task.dto.PutTaskDto;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    GetTaskDto taskToGetTaskDto(Task task);

    Task postTaskDtoToTask(PostTaskDto postTaskDto);

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
