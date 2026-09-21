package com.foodlogistics.erp.inbound.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderItemResponse;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderResponse;
import com.foodlogistics.erp.inbound.mapper.InboundMapper;
import com.foodlogistics.erp.inbound.validator.InboundValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboundService {

    // 입고 대상 발주와 품목을 Oracle에서 조회
    private final InboundMapper inboundMapper;

    // 로그인 정보와 발주 ID 검증
    private final InboundValidator inboundValidator;

    // 현재 회사의 입고 대상 발주 목록 조회
    @Transactional(readOnly = true)
    public List<InboundPurchaseOrderResponse>
    getInboundTargetPurchaseOrders(
            Long companyId,
            Long appUserId
    ) {
        inboundValidator.validateAuthenticatedUser(
                companyId,
                appUserId
        );

        log.info(
                "Inbound target purchase order search started: companyId={}, appUserId={}",
                companyId,
                appUserId
        );

        List<InboundPurchaseOrderResponse> response =
                inboundMapper.findInboundTargetPurchaseOrders(
                        companyId
                );

        log.info(
                "Inbound target purchase order search completed: companyId={}, count={}",
                companyId,
                response.size()
        );

        return response;
    }

    // 선택한 발주의 아직 입고 가능한 품목 조회
    @Transactional(readOnly = true)
    public List<InboundPurchaseOrderItemResponse>
    getInboundTargetPurchaseOrderItems(
            Long companyId,
            Long appUserId,
            Long purchaseOrderId
    ) {
        inboundValidator.validateAuthenticatedUser(
                companyId,
                appUserId
        );

        inboundValidator.validatePurchaseOrderId(
                purchaseOrderId
        );

        log.info(
                "Inbound target purchase order item search started: "
                        + "companyId={}, appUserId={}, purchaseOrderId={}",
                companyId,
                appUserId,
                purchaseOrderId
        );

        List<InboundPurchaseOrderItemResponse> items =
                inboundMapper.findInboundTargetPurchaseOrderItems(
                        companyId,
                        purchaseOrderId
                );

        log.info(
                "Inbound target purchase order item search completed: "
                        + "companyId={}, purchaseOrderId={}, count={}",
                companyId,
                purchaseOrderId,
                items.size()
        );

        if (items.isEmpty()) {
            log.info(
                    "No inbound target purchase order items found: "
                            + "companyId={}, purchaseOrderId={}",
                    companyId,
                    purchaseOrderId
            );

            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "입고 가능한 발주 품목을 찾을 수 없습니다."
            );
        }

        return items;
    }
}