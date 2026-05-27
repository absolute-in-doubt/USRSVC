package com.innowise.userservice.application.mapper.impl;


import com.innowise.userservice.application.dto.PageResponseDto;
import com.innowise.userservice.application.mapper.PageMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PageMapperImpl implements PageMapper {

    @Override
    public <T> PageResponseDto<T> toDto(Page<T> entity) {
        Pageable pageable = entity.getPageable();
        return new PageResponseDto<>(
                entity.getContent(),
                entity.isEmpty(),
                entity.isFirst(),
                entity.isLast(),
                entity.getNumber(),
                entity.getNumberOfElements(),
                entity.getSize(),
                entity.getTotalElements(),
                entity.getTotalPages()
        );
    }
}
