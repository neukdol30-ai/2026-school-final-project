package com.foodlogistics.erp.outbound.controller;


import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.outbound.dto.OutboundCreateRequestDto;
import com.foodlogistics.erp.outbound.dto.OutboundResponseDto;
import com.foodlogistics.erp.outbound.service.OutboundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/outbounds")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class OutboundController {

    private final OutboundService outboundService;

    @PostMapping
    public ApiResponse<OutboundResponseDto> createOutbound(
            @RequestBody
            @Valid
            OutboundCreateRequestDto request
    ) {
        return ApiResponse.ok(
                outboundService.createOutbound(request)
        );
    }
    @PostMapping("/{outboundId}/confirm")
    public ApiResponse<OutboundResponseDto> confirmOutbound(
            @PathVariable Long outboundId
    ) {
        return ApiResponse.ok(
                outboundService.confirmOutbound(outboundId)
        );
    }
}
