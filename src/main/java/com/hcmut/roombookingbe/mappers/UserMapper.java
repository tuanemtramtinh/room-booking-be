package com.hcmut.roombookingbe.mappers;

import com.hcmut.roombookingbe.dtos.response.UserResponse;
import com.hcmut.roombookingbe.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "role", expression = "java(user.getRole().name())")
  @Mapping(target = "status", expression = "java(user.getStatus().name())")
  UserResponse toUserResponse(User user);
}
