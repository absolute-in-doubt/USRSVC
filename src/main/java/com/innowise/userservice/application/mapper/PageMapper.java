package com.innowise.userservice.application.mapper;

import com.innowise.userservice.application.dto.PageResponseDto;
import org.springframework.data.domain.Page;

public interface PageMapper {

    <T> PageResponseDto<T> toDto(Page<T> entity);
}
