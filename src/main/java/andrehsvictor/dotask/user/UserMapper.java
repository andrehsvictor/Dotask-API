package andrehsvictor.dotask.user;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import andrehsvictor.dotask.user.dto.GetUserDto;
import andrehsvictor.dotask.user.dto.PostUserDto;
import andrehsvictor.dotask.user.dto.PutUserDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    GetUserDto userToGetUserDto(User user);

    User postUserDtoToUser(PostUserDto postUserDto);

    User updateUserFromPutUserDto(PutUserDto putUserDto, @MappingTarget User user);

}
