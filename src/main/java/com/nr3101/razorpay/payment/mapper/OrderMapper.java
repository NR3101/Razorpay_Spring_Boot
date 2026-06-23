package com.nr3101.razorpay.payment.mapper;

import com.nr3101.razorpay.payment.dto.response.OrderResponse;
import com.nr3101.razorpay.payment.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    OrderResponse toResponse(OrderRecord order);
}
