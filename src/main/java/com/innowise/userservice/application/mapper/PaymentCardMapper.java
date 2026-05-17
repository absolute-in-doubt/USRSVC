package com.innowise.userservice.application.mapper;

import com.innowise.userservice.application.dto.CreatePaymentCardDto;
import com.innowise.userservice.application.dto.PaymentCardResponseDto;
import com.innowise.userservice.domain.model.PaymentCard;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentCardMapper {
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    PaymentCard toEntity(CreatePaymentCardDto createPaymentCardDto);

    PaymentCardResponseDto toDto(PaymentCard paymentCard);

    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    PaymentCard updateEntity(PaymentCard updatedPaymentCard, @MappingTarget PaymentCard paymentCard);

    List<PaymentCardResponseDto> toDtoList(List<PaymentCard> cards);
}
