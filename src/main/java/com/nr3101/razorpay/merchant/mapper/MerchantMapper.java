package com.nr3101.razorpay.merchant.mapper;

import com.nr3101.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.nr3101.razorpay.merchant.dto.response.MerchantResponse;
import com.nr3101.razorpay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {

    Merchant toEntityFromSignUpRequest(MerchantSignupRequest request);

    MerchantResponse toResponse(Merchant merchant);
}
