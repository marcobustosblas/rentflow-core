package com.marco.rentflow.infrastructure.adapters.in.web.payment.mapper;

import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentCheckoutResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentRestMapper {

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "checkoutUrl", ignore = true) // generado por Stripe/Webpay
    PaymentCheckoutResponseDTO toDto(PaymentRecord paymentRecord);

    // ---Mapeo para el C10 (Historial y Detalles de Pago) ---

    @Mapping(target = "expectedAmount", source = "expectedAmount.amount")
    @Mapping(target = "currency", source = "expectedAmount.currency")
    @Mapping(target = "amountPaid", source = "amountPaid.amount")
    @Mapping(target = "lateFeeApplied", source = "lateFeeApplied.amount")
    PaymentResponseDTO toResponseDto(PaymentRecord paymentRecord);

    // MapStruct generará automáticamente la lista usando el método de arriba
    List<PaymentResponseDTO> toResponseDtoList(List<PaymentRecord> paymentRecords);

}
