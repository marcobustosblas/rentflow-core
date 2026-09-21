package com.marco.rentflow.infrastructure.adapters.in.web.payment.mapper;

import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentCheckoutResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentRestMapper {

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "checkoutUrl", ignore = true) // generado por Stripe/Webpay
    PaymentCheckoutResponseDTO toDto(PaymentRecord paymentRecord);

}
