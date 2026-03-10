package com.waitless.user.mapper;

import com.waitless.user.dto.CreateUserRequest;
import com.waitless.user.dto.UserDTO;
import com.waitless.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    User toEntity(CreateUserRequest request);
}
