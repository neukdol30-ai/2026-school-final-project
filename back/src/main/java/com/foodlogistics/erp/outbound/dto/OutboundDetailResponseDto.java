package com.foodlogistics.erp.outbound.dto;

import java.util.List;

public record OutboundDetailResponseDto(
        OutboundResponseDto outbound,
        List<OutboundItemResponseDto> items
) {
}
