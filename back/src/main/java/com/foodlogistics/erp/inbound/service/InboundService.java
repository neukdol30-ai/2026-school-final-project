package com.foodlogistics.erp.inbound.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.inbound.dto.InboundCreateRequest;
import com.foodlogistics.erp.inbound.dto.InboundCreateResponse;
import com.foodlogistics.erp.inbound.dto.InboundDetailResponse;
import com.foodlogistics.erp.inbound.dto.InboundItemDetailResponse;
import com.foodlogistics.erp.inbound.dto.InboundItemLotDetailResponse;
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

    private final InboundMapper inboundMapper;

    private final InboundValidator inboundValidator;

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
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "입고 가능한 발주 품목을 찾을 수 없습니다."
            );
        }

        return items;
    }

    @Transactional(readOnly = true)
    public InboundDetailResponse getInboundDetail(
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

        InboundDetailResponse response =
                inboundMapper.findInboundDetail(
                                companyId,
                                inboundId
                        )
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.RESOURCE_NOT_FOUND,
                                        "입고서를 찾을 수 없습니다."
                                )
                        );

        List<InboundItemDetailResponse> items =
                inboundMapper.findInboundDetailItems(
                        companyId,
                        inboundId
                );

        List<InboundItemLotDetailResponse> lots =
                inboundMapper.findInboundDetailLots(
                        companyId,
                        inboundId
                );

        Map<Long, List<InboundItemLotDetailResponse>> lotsByInboundItemId =
                new HashMap<>();

        for (InboundItemLotDetailResponse lot : lots) {
            lotsByInboundItemId
                    .computeIfAbsent(
                            lot.getInboundItemId(),
                            key -> new ArrayList<>()
                    )
                    .add(lot);
        }

        for (InboundItemDetailResponse item : items) {
            item.setLots(
                    lotsByInboundItemId.getOrDefault(
                            item.getInboundItemId(),
                            new ArrayList<>()
                    )
            );
        }

        response.setItems(
                items
        );

        return response;
    }

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

        List<InboundItemInsertParam> itemParams =
                new ArrayList<>();

        Map<Long, InboundPurchaseOrderItemResponse> itemReferences =
                new HashMap<>();

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

        inboundMapper.deleteInboundItemLots(
                companyId,
                inboundId
        );

        inboundMapper.deleteInboundItems(
                companyId,
                inboundId
        );

        int savedItemCount =
                0;

        int savedLotCount =
                0;

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

    @Transactional
    public Long confirmInbound(
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

        log.info(
                "Inbound confirmation started: "
                        + "companyId={}, appUserId={}, inboundId={}",
                companyId,
                appUserId,
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

        if (purchaseOrder.getWarehouseId() == null
                || purchaseOrder.getWarehouseId() <= 0) {

            throw new IllegalStateException(
                    "입고 창고 정보를 확인할 수 없습니다."
            );
        }

        Long warehouseId =
                purchaseOrder.getWarehouseId();

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

        List<InboundConfirmLotInfo> confirmLots =
                inboundMapper.findInboundConfirmLots(
                        companyId,
                        purchaseOrderId,
                        inboundId
                );

        for (InboundConfirmItemInfo item : confirmItems) {

            int updatedCount =
                    inboundMapper.increasePurchaseOrderItemReceivedQty(
                            companyId,
                            purchaseOrderId,
                            item.getPurchaseOrderItemId(),
                            item.getBaseReceivedQty()
                    );

            inboundValidator.validatePurchaseOrderItemUpdateCount(
                    updatedCount
            );
        }

        applyInboundInventoryAndSaveHistory(
                companyId,
                warehouseId,
                inboundId,
                appUserId,
                confirmItems,
                confirmLots
        );

        int remainingItemCount =
                inboundMapper.countRemainingPurchaseOrderItems(
                        purchaseOrderId
                );

        String receiptStatus =
                remainingItemCount == 0
                        ? "RECEIVED"
                        : "PARTIAL";

        int purchaseOrderUpdatedCount =
                inboundMapper.updatePurchaseOrderReceiptStatus(
                        companyId,
                        purchaseOrderId,
                        receiptStatus,
                        appUserId
                );

        inboundValidator.validatePurchaseOrderStatusUpdateCount(
                purchaseOrderUpdatedCount
        );

        int inboundUpdatedCount =
                inboundMapper.confirmInbound(
                        companyId,
                        purchaseOrderId,
                        inboundId,
                        appUserId
                );

        inboundValidator.validateInboundConfirmUpdateCount(
                inboundUpdatedCount
        );

        log.info(
                "Inbound confirmation completed: "
                        + "companyId={}, appUserId={}, inboundId={}, "
                        + "purchaseOrderId={}, receiptStatus={}",
                companyId,
                appUserId,
                inboundId,
                purchaseOrderId,
                receiptStatus
        );

        return inboundId;
    }

    private void applyInboundInventoryAndSaveHistory(
            Long companyId,
            Long warehouseId,
            Long inboundId,
            Long appUserId,
            List<InboundConfirmItemInfo> confirmItems,
            List<InboundConfirmLotInfo> confirmLots
    ) {
        Map<Long, List<InboundConfirmLotInfo>> lotsByInboundItemId =
                groupInboundLotsByItemId(
                        confirmLots
                );

        for (InboundConfirmItemInfo item : confirmItems) {

            List<InboundConfirmLotInfo> itemLots =
                    lotsByInboundItemId.getOrDefault(
                            item.getInboundItemId(),
                            List.of()
                    );

            if ("Y".equals(
                    item.getLotManagedYn()
            )) {

                for (InboundConfirmLotInfo lot : itemLots) {

                    increaseInboundLotStock(
                            companyId,
                            warehouseId,
                            item.getProductId(),
                            lot.getLotId(),
                            lot.getBaseLotQty()
                    );
                }

                increaseInboundStock(
                        companyId,
                        warehouseId,
                        item.getProductId(),
                        item.getBaseReceivedQty()
                );

                for (InboundConfirmLotInfo lot : itemLots) {

                    insertInboundStockHistory(
                            companyId,
                            warehouseId,
                            item.getProductId(),
                            lot.getLotId(),
                            lot.getBaseLotQty(),
                            inboundId,
                            appUserId
                    );
                }

                continue;
            }

            if (!itemLots.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 비관리상품에는 LOT 재고정보가 존재할 수 없습니다."
                );
            }

            increaseInboundStock(
                    companyId,
                    warehouseId,
                    item.getProductId(),
                    item.getBaseReceivedQty()
            );

            insertInboundStockHistory(
                    companyId,
                    warehouseId,
                    item.getProductId(),
                    null,
                    item.getBaseReceivedQty(),
                    inboundId,
                    appUserId
            );
        }
    }

    private Map<Long, List<InboundConfirmLotInfo>>
    groupInboundLotsByItemId(
            List<InboundConfirmLotInfo> confirmLots
    ) {
        Map<Long, List<InboundConfirmLotInfo>> lotsByInboundItemId =
                new HashMap<>();

        for (InboundConfirmLotInfo lot : confirmLots) {

            lotsByInboundItemId
                    .computeIfAbsent(
                            lot.getInboundItemId(),
                            ignored -> new ArrayList<>()
                    )
                    .add(
                            lot
                    );
        }

        return lotsByInboundItemId;
    }

    private void increaseInboundLotStock(
            Long companyId,
            Long warehouseId,
            Long productId,
            Long lotId,
            BigDecimal baseLotQty
    ) {
        int updatedCount =
                inboundMapper.increaseInboundLotStock(
                        companyId,
                        warehouseId,
                        productId,
                        lotId,
                        baseLotQty
                );

        if (updatedCount == 1) {
            return;
        }

        try {

            int insertedCount =
                    inboundMapper.insertInboundLotStock(
                            companyId,
                            warehouseId,
                            productId,
                            lotId,
                            baseLotQty
                    );

            if (insertedCount != 1) {
                throw new IllegalStateException(
                        "LOT 재고 생성 결과를 확인할 수 없습니다."
                );
            }

        } catch (DuplicateKeyException e) {

            int retryUpdatedCount =
                    inboundMapper.increaseInboundLotStock(
                            companyId,
                            warehouseId,
                            productId,
                            lotId,
                            baseLotQty
                    );

            if (retryUpdatedCount != 1) {
                throw new IllegalStateException(
                        "LOT 재고 증가 결과를 확인할 수 없습니다."
                );
            }
        }
    }

    private void increaseInboundStock(
            Long companyId,
            Long warehouseId,
            Long productId,
            BigDecimal baseReceivedQty
    ) {
        int updatedCount =
                inboundMapper.increaseInboundStock(
                        companyId,
                        warehouseId,
                        productId,
                        baseReceivedQty
                );

        if (updatedCount == 1) {
            return;
        }

        try {

            int insertedCount =
                    inboundMapper.insertInboundStock(
                            companyId,
                            warehouseId,
                            productId,
                            baseReceivedQty
                    );

            if (insertedCount != 1) {
                throw new IllegalStateException(
                        "재고 생성 결과를 확인할 수 없습니다."
                );
            }

        } catch (DuplicateKeyException e) {

            int retryUpdatedCount =
                    inboundMapper.increaseInboundStock(
                            companyId,
                            warehouseId,
                            productId,
                            baseReceivedQty
                    );

            if (retryUpdatedCount != 1) {
                throw new IllegalStateException(
                        "재고 증가 결과를 확인할 수 없습니다."
                );
            }
        }
    }

    private void insertInboundStockHistory(
            Long companyId,
            Long warehouseId,
            Long productId,
            Long lotId,
            BigDecimal changeQty,
            Long inboundId,
            Long appUserId
    ) {
        int insertedCount =
                inboundMapper.insertInboundStockHistory(
                        companyId,
                        warehouseId,
                        productId,
                        lotId,
                        changeQty,
                        inboundId,
                        appUserId
                );

        if (insertedCount != 1) {
            throw new IllegalStateException(
                    "입고 재고이력 저장 결과를 확인할 수 없습니다."
            );
        }
    }

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

    private String createLotKey(
            Long productId,
            String lotNo
    ) {
        return productId
                + ":"
                + lotNo;
    }

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
}