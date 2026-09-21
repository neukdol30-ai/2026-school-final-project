package com.foodlogistics.erp.inbound.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.inbound.dto.InboundCreateRequest;
import com.foodlogistics.erp.inbound.dto.InboundCreateResponse;
import com.foodlogistics.erp.inbound.dto.InboundItemLotRequest;
import com.foodlogistics.erp.inbound.dto.InboundItemUpdateRequest;
import com.foodlogistics.erp.inbound.dto.InboundItemsUpdateRequest;
import com.foodlogistics.erp.inbound.dto.InboundItemsUpdateResponse;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderItemResponse;
import com.foodlogistics.erp.inbound.dto.InboundPurchaseOrderResponse;
import com.foodlogistics.erp.inbound.mapper.*;
import com.foodlogistics.erp.inbound.validator.InboundValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboundService {

    // 입고 조회, DRAFT 생성, 입고품목 저장에 사용하는 MyBatis Mapper
    private final InboundMapper inboundMapper;

    // 입고 업무 규칙 검증
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

    // DRAFT 입고서의 품목 전체를 교체 저장
    @Transactional
    public InboundItemsUpdateResponse updateInboundItems(
            Long companyId,
            Long appUserId,
            Long inboundId,
            InboundItemsUpdateRequest request
    ) {
        inboundValidator.validateAuthenticatedUser(
                companyId,
                appUserId
        );

        inboundValidator.validateInboundId(
                inboundId
        );

        inboundValidator.validateItemRequests(
                request.getItems()
        );

        log.info(
                "Inbound item update started: "
                        + "companyId={}, appUserId={}, inboundId={}, itemCount={}",
                companyId,
                appUserId,
                inboundId,
                request.getItems().size()
        );

        // 먼저 INBOUND를 일반 조회하여 연결된 발주 ID를 확인
        InboundItemUpdateTargetInfo target =
                inboundMapper.findInboundItemUpdateTarget(
                        companyId,
                        inboundId
                );

        inboundValidator.validateInboundExists(
                target
        );

        Long purchaseOrderId =
                target.getPurchaseOrderId();

        if (purchaseOrderId == null
                || purchaseOrderId <= 0) {

            throw new IllegalStateException(
                    "입고서의 발주 정보를 확인할 수 없습니다."
            );
        }

        // 7/10과 잠금 순서를 맞추기 위해 PURCHASE_ORDER를 먼저 잠금
        InboundPurchaseOrderLockInfo purchaseOrder =
                inboundMapper.findPurchaseOrderForUpdate(
                        companyId,
                        purchaseOrderId
                );

        inboundValidator.validatePurchaseOrderForInbound(
                purchaseOrder
        );

        // PURCHASE_ORDER 다음으로 INBOUND를 잠금
        InboundItemUpdateTargetInfo lockedInbound =
                inboundMapper.findInboundForUpdate(
                        companyId,
                        inboundId,
                        purchaseOrderId
                );

        inboundValidator.validateInboundDraft(
                lockedInbound
        );

        List<InboundItemInsertParam> itemParams =
                new ArrayList<>();

        Map<Long, InboundPurchaseOrderItemResponse> itemReferences =
                new HashMap<>();

        // 모든 품목을 먼저 검증하고 계산
        for (InboundItemUpdateRequest itemRequest : request.getItems()) {

            InboundPurchaseOrderItemResponse reference =
                    inboundMapper.findInboundPurchaseOrderItem(
                            companyId,
                            purchaseOrderId,
                            itemRequest.getPurchaseOrderItemId()
                    );

            inboundValidator.validateInboundPurchaseOrderItem(
                    reference
            );

            BigDecimal receivedQty =
                    itemRequest.getReceivedQty()
                            .setScale(
                                    3,
                                    RoundingMode.UNNECESSARY
                            );

            BigDecimal conversionQty =
                    reference.getConversionQty()
                            .setScale(
                                    3,
                                    RoundingMode.UNNECESSARY
                            );

            BigDecimal baseReceivedQty =
                    receivedQty
                            .multiply(
                                    conversionQty
                            );

            inboundValidator.validateBaseReceivedQty(
                    baseReceivedQty,
                    reference.getRemainingBaseQty()
            );

            baseReceivedQty =
                    baseReceivedQty.setScale(
                            3,
                            RoundingMode.UNNECESSARY
                    );

            inboundValidator.validateLotPolicy(
                    reference.getLotManagedYn(),
                    itemRequest.getLots(),
                    baseReceivedQty
            );

            InboundItemInsertParam itemParam =
                    new InboundItemInsertParam();

            itemParam.setInboundId(
                    inboundId
            );

            itemParam.setPurchaseOrderItemId(
                    reference.getPurchaseOrderItemId()
            );

            itemParam.setProductUnitId(
                    reference.getProductUnitId()
            );

            itemParam.setReceivedQty(
                    receivedQty
            );

            itemParam.setConversionQty(
                    conversionQty
            );

            itemParam.setBaseReceivedQty(
                    baseReceivedQty
            );

            itemParams.add(
                    itemParam
            );

            itemReferences.put(
                    itemRequest.getPurchaseOrderItemId(),
                    reference
            );
        }

        // 같은 신규 LOT를 여러 요청이 동시에 만들 때
        // 잠금 순서를 일정하게 유지하기 위해 상품 ID + LOT 번호 순서로 정렬
        Map<String, LotResolveTarget> uniqueLotTargets =
                new LinkedHashMap<>();

        for (InboundItemUpdateRequest itemRequest : request.getItems()) {

            InboundPurchaseOrderItemResponse reference =
                    itemReferences.get(
                            itemRequest.getPurchaseOrderItemId()
                    );

            if (!"Y".equals(
                    reference.getLotManagedYn()
            )) {
                continue;
            }

            for (InboundItemLotRequest lotRequest : itemRequest.getLots()) {

                String normalizedLotNo =
                        lotRequest.getLotNo()
                                .trim();

                String lotKey =
                        createLotKey(
                                reference.getProductId(),
                                normalizedLotNo
                        );

                uniqueLotTargets.putIfAbsent(
                        lotKey,
                        new LotResolveTarget(
                                reference.getProductId(),
                                normalizedLotNo,
                                lotRequest
                        )
                );
            }
        }

        List<LotResolveTarget> sortedLotTargets =
                new ArrayList<>(
                        uniqueLotTargets.values()
                );

        sortedLotTargets.sort(
                Comparator
                        .comparing(
                                LotResolveTarget::getProductId
                        )
                        .thenComparing(
                                LotResolveTarget::getLotNo
                        )
        );

        Map<String, LotInfo> resolvedLots =
                new HashMap<>();

        // 기존 LOT는 재사용하고, 없는 LOT만 새로 생성
        for (LotResolveTarget lotTarget : sortedLotTargets) {

            LotInfo resolvedLot =
                    resolveLot(
                            companyId,
                            lotTarget
                    );

            resolvedLots.put(
                    createLotKey(
                            lotTarget.getProductId(),
                            lotTarget.getLotNo()
                    ),
                    resolvedLot
            );
        }

        // 같은 LOT가 여러 품목에서 사용됐다면
        // 각각의 요청 날짜가 기존 LOT 정보와 충돌하지 않는지 모두 검사
        for (InboundItemUpdateRequest itemRequest : request.getItems()) {

            InboundPurchaseOrderItemResponse reference =
                    itemReferences.get(
                            itemRequest.getPurchaseOrderItemId()
                    );

            if (!"Y".equals(
                    reference.getLotManagedYn()
            )) {
                continue;
            }

            for (InboundItemLotRequest lotRequest : itemRequest.getLots()) {

                String normalizedLotNo =
                        lotRequest.getLotNo()
                                .trim();

                String lotKey =
                        createLotKey(
                                reference.getProductId(),
                                normalizedLotNo
                        );

                LotInfo resolvedLot =
                        resolvedLots.get(
                                lotKey
                        );

                if (resolvedLot == null) {
                    throw new IllegalStateException(
                            "LOT 정보를 확인할 수 없습니다."
                    );
                }

                inboundValidator.validateExistingLotDates(
                        resolvedLot,
                        lotRequest
                );
            }
        }

        // 모든 검증이 끝난 다음 기존 자식 LOT 연결부터 삭제
        inboundMapper.deleteInboundItemLots(
                companyId,
                inboundId
        );

        // 그다음 기존 입고품목 전체 삭제
        inboundMapper.deleteInboundItems(
                companyId,
                inboundId
        );

        int savedItemCount =
                0;

        int savedLotCount =
                0;

        // 검증이 끝난 새 입고품목을 다시 저장
        for (int index = 0;
             index < itemParams.size();
             index++) {

            InboundItemInsertParam itemParam =
                    itemParams.get(
                            index
                    );

            InboundItemUpdateRequest itemRequest =
                    request.getItems()
                            .get(
                                    index
                            );

            InboundPurchaseOrderItemResponse reference =
                    itemReferences.get(
                            itemRequest.getPurchaseOrderItemId()
                    );

            int insertedItemCount =
                    inboundMapper.insertInboundItem(
                            itemParam
                    );

            if (insertedItemCount != 1
                    || itemParam.getInboundItemId() == null) {

                throw new IllegalStateException(
                        "입고 품목 저장 결과를 확인할 수 없습니다."
                );
            }

            savedItemCount++;

            // LOT 비관리상품은 INBOUND_ITEM_LOT를 만들지 않음
            if (!"Y".equals(
                    reference.getLotManagedYn()
            )) {
                continue;
            }

            for (InboundItemLotRequest lotRequest : itemRequest.getLots()) {

                String normalizedLotNo =
                        lotRequest.getLotNo()
                                .trim();

                String lotKey =
                        createLotKey(
                                reference.getProductId(),
                                normalizedLotNo
                        );

                LotInfo lotInfo =
                        resolvedLots.get(
                                lotKey
                        );

                if (lotInfo == null
                        || lotInfo.getLotId() == null) {

                    throw new IllegalStateException(
                            "저장할 LOT ID를 확인할 수 없습니다."
                    );
                }

                BigDecimal baseLotQty =
                        lotRequest.getBaseLotQty()
                                .setScale(
                                        3,
                                        RoundingMode.UNNECESSARY
                                );

                int insertedLotCount =
                        inboundMapper.insertInboundItemLot(
                                itemParam.getInboundItemId(),
                                lotInfo.getLotId(),
                                baseLotQty
                        );

                if (insertedLotCount != 1) {
                    throw new IllegalStateException(
                            "입고품목 LOT 저장에 실패했습니다."
                    );
                }

                savedLotCount++;
            }
        }

        log.info(
                "Inbound item update completed: "
                        + "companyId={}, appUserId={}, inboundId={}, "
                        + "savedItemCount={}, savedLotCount={}",
                companyId,
                appUserId,
                inboundId,
                savedItemCount,
                savedLotCount
        );

        return new InboundItemsUpdateResponse(
                inboundId,
                savedItemCount,
                savedLotCount
        );
    }

    // 기존 LOT를 조회하고 없으면 신규 LOT 생성
    private LotInfo resolveLot(
            Long companyId,
            LotResolveTarget target
    ) {
        LotInfo existingLot =
                inboundMapper.findLot(
                        companyId,
                        target.getProductId(),
                        target.getLotNo()
                );

        if (existingLot != null) {

            inboundValidator.validateExistingLotDates(
                    existingLot,
                    target.getRequest()
            );

            return existingLot;
        }

        LotInsertParam insertParam =
                new LotInsertParam();

        insertParam.setCompanyId(
                companyId
        );

        insertParam.setProductId(
                target.getProductId()
        );

        insertParam.setLotNo(
                target.getLotNo()
        );

        insertParam.setManufactureDate(
                target.getRequest()
                        .getManufactureDate()
        );

        insertParam.setExpiryDate(
                target.getRequest()
                        .getExpiryDate()
        );

        try {

            int insertedCount =
                    inboundMapper.insertLot(
                            insertParam
                    );

            if (insertedCount != 1
                    || insertParam.getLotId() == null) {

                throw new IllegalStateException(
                        "신규 LOT 저장 결과를 확인할 수 없습니다."
                );
            }

            LotInfo createdLot =
                    new LotInfo();

            createdLot.setLotId(
                    insertParam.getLotId()
            );

            createdLot.setCompanyId(
                    companyId
            );

            createdLot.setProductId(
                    target.getProductId()
            );

            createdLot.setLotNo(
                    target.getLotNo()
            );

            createdLot.setManufactureDate(
                    target.getRequest()
                            .getManufactureDate()
            );

            createdLot.setExpiryDate(
                    target.getRequest()
                            .getExpiryDate()
            );

            return createdLot;

        } catch (DuplicateKeyException e) {

            // 다른 트랜잭션이 같은 상품 + 같은 LOT 번호를
            // 먼저 생성한 경우 다시 조회해서 그 LOT를 재사용
            LotInfo concurrentLot =
                    inboundMapper.findLot(
                            companyId,
                            target.getProductId(),
                            target.getLotNo()
                    );

            if (concurrentLot == null) {
                throw e;
            }

            inboundValidator.validateExistingLotDates(
                    concurrentLot,
                    target.getRequest()
            );

            return concurrentLot;
        }
    }

    // 상품 ID와 LOT 번호를 묶어 Map Key 생성
    private String createLotKey(
            Long productId,
            String lotNo
    ) {
        return productId
                + ":"
                + lotNo;
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

    // LOT 생성 순서를 일정하게 유지하기 위한 Service 내부 객체
    private static class LotResolveTarget {

        private final Long productId;

        private final String lotNo;

        private final InboundItemLotRequest request;

        private LotResolveTarget(
                Long productId,
                String lotNo,
                InboundItemLotRequest request
        ) {
            this.productId =
                    productId;

            this.lotNo =
                    lotNo;

            this.request =
                    request;
        }

        private Long getProductId() {
            return productId;
        }

        private String getLotNo() {
            return lotNo;
        }

        private InboundItemLotRequest getRequest() {
            return request;
        }
    }

    private List<InboundConfirmItemInfo> prepareInboundConfirmation(
            Long companyId,
            Long appUserId,
            Long inboundId
    ) {
        inboundValidator.validateAuthenticatedUser(
                companyId,
                appUserId
        );

        inboundValidator.validateInboundId(
                inboundId
        );

        InboundItemUpdateTargetInfo inboundTarget =
                inboundMapper.findInboundItemUpdateTarget(
                        companyId,
                        inboundId
                );

        inboundValidator.validateInboundExists(
                inboundTarget
        );

        Long purchaseOrderId =
                inboundTarget.getPurchaseOrderId();

        inboundValidator.validatePurchaseOrderId(
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

        InboundItemUpdateTargetInfo lockedInbound =
                inboundMapper.findInboundForUpdate(
                        companyId,
                        inboundId,
                        purchaseOrderId
                );

        inboundValidator.validateInboundDraft(
                lockedInbound
        );

        List<InboundConfirmItemInfo> confirmItems =
                inboundMapper.findInboundConfirmItems(
                        companyId,
                        purchaseOrderId,
                        inboundId
                );

        inboundValidator.validateInboundConfirmItems(
                confirmItems
        );

        return confirmItems;
    }
}