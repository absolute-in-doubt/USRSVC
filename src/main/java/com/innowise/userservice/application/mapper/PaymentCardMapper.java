package com.innowise.userservice.application.mapper;

import com.innowise.userservice.application.dto.CreatePaymentCardDto;
import com.innowise.userservice.application.dto.PaymentCardResponseDto;
import com.innowise.userservice.application.dto.UpdatePaymentCardDto;
import com.innowise.userservice.domain.model.PaymentCard;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentCardMapper {
    @Mapping(target = "active", constant = "true")
    PaymentCard toEntity(CreatePaymentCardDto createPaymentCardDto);

    PaymentCardResponseDto toDto(PaymentCard paymentCard);

    PaymentCard updateEntity(UpdatePaymentCardDto updatedPaymentCardDto, @MappingTarget PaymentCard paymentCard);

    List<PaymentCardResponseDto> toDtoList(List<PaymentCard> cards);
}
