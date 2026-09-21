package com.foodlogistics.erp.inbound.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.inbound.dto.InboundCreateRequest;
import com.foodlogistics.erp.inbound.dto.InboundCreateResponse;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderItemResponse;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderResponse;
import com.foodlogistics.erp.inbound.mapper.InboundInsertParam;
import com.foodlogistics.erp.inbound.mapper.InboundMapper;
import com.foodlogistics.erp.inbound.mapper.InboundPurchaseOrderLockInfo;
import com.foodlogistics.erp.inbound.validator.InboundValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboundService {

    // 입고 대상 발주와 품목 조회 및 입고 Header 저장
    private final InboundMapper inboundMapper;

    // 입고 관련 업무 규칙 검증
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

    // INBOUND DRAFT Header 생성
    @Transactional
    public InboundCreateResponse createInbound(
            Long companyId,
            Long appUserId,
            InboundCreateRequest request
    ) {
        inboundValidator.validateAuthenticatedUser(
                companyId,
                appUserId
        );

        inboundValidator.validatePurchaseOrderId(
                request.getPurchaseOrderId()
        );

        Long purchaseOrderId =
                request.getPurchaseOrderId();

        log.info(
                "Inbound draft creation started: "
                        + "companyId={}, appUserId={}, purchaseOrderId={}",
                companyId,
                appUserId,
                purchaseOrderId
        );

        InboundPurchaseOrderLockInfo purchaseOrder =
                inboundMapper.findPurchaseOrderForUpdate(
                        companyId,
                        purchaseOrderId
                );

        inboundValidator.validatePurchaseOrderForInbound(
                purchaseOrder
        );

        int remainingItemCount =
                inboundMapper.countRemainingPurchaseOrderItems(
                        purchaseOrderId
                );

        inboundValidator.validateRemainingPurchaseOrderItems(
                remainingItemCount
        );

        LocalDate latestConfirmedInboundDate =
                inboundMapper.findLatestConfirmedInboundDate(
                        companyId,
                        purchaseOrderId
                );

        inboundValidator.validateInboundDate(
                request.getInboundDate(),
                purchaseOrder.getOrderDate(),
                latestConfirmedInboundDate
        );

        int draftCount =
                inboundMapper.countDraftInbounds(
                        companyId,
                        purchaseOrderId
                );

        inboundValidator.validateNoExistingDraft(
                draftCount
        );

        Long sequence =
                inboundMapper.nextInboundNoSequence();

        if (sequence == null
                || sequence <= 0) {

            throw new IllegalStateException(
                    "입고번호 Sequence 값을 생성할 수 없습니다."
            );
        }

        String inboundNo =
                createInboundNo(
                        request.getInboundDate(),
                        sequence
                );

        InboundInsertParam insertParam =
                new InboundInsertParam();

        insertParam.setCompanyId(
                companyId
        );

        insertParam.setInboundNo(
                inboundNo
        );

        insertParam.setPurchaseOrderId(
                purchaseOrderId
        );

        insertParam.setWarehouseId(
                purchaseOrder.getWarehouseId()
        );

        insertParam.setInboundDate(
                request.getInboundDate()
        );

        insertParam.setMemo(
                request.getMemo()
        );

        insertParam.setCreatedBy(
                appUserId
        );

        int insertedCount =
                inboundMapper.insertInbound(
                        insertParam
                );

        if (insertedCount != 1
                || insertParam.getInboundId() == null) {

            throw new IllegalStateException(
                    "입고 DRAFT 저장 결과를 확인할 수 없습니다."
            );
        }

        log.info(
                "Inbound draft creation completed: "
                        + "companyId={}, appUserId={}, "
                        + "purchaseOrderId={}, inboundId={}, inboundNo={}",
                companyId,
                appUserId,
                purchaseOrderId,
                insertParam.getInboundId(),
                inboundNo
        );

        return new InboundCreateResponse(
                insertParam.getInboundId(),
                inboundNo,
                purchaseOrderId,
                purchaseOrder.getWarehouseId(),
                request.getInboundDate(),
                "DRAFT"
        );
    }

    // 입고일 + Sequence를 업무용 입고번호로 변환
    private String createInboundNo(
            LocalDate inboundDate,
            Long sequence
    ) {
        String inboundDateText =
                inboundDate.format(
                        DateTimeFormatter.BASIC_ISO_DATE
                );

        String sequenceText =
                String.format(
                        "%06d",
                        sequence
                );

        return "IN-"
                + inboundDateText
                + "-"
                + sequenceText;
    }
}