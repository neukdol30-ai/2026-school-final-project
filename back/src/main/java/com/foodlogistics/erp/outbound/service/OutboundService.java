package com.foodlogistics.erp.outbound.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.outbound.dto.*;
import com.foodlogistics.erp.outbound.mapper.OutboundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OutboundService {

    private final OutboundMapper outboundMapper;
    private final OutboundValidator outboundValidator;
    private final OutboundCalculator outboundCalculator;
    private final OutboundNumberGenerator outboundNumberGenerator;

    @Transactional
    public OutboundResponseDto createOutbound(
            Long companyId,
            Long appUserId,
            OutboundCreateRequestDto request
    ) {
        outboundValidator.validateUsableWarehouse(
                companyId,
                request.warehouseId()
        );

        List<OutboundItemSaveDto> itemsToSave =
                prepareItemsToSave(companyId, request);

        OutboundSaveDto outboundToSave = createOutboundSaveDto(
                companyId,
                appUserId,
                request
        );

        outboundMapper.insertOutbound(outboundToSave);

        saveOutboundItems(
                outboundToSave.getOutboundId(),
                itemsToSave,
                appUserId
        );

        return outboundMapper.findById(
                companyId,
                outboundToSave.getOutboundId()
        );

    }

    // 작성중 출고서는 재고에 아직 반영되지 않았으므로 품목·LOT 배정을 다시 저장할 수 있다.
    @Transactional
    public OutboundResponseDto updateOutbound(
            Long companyId,
            Long appUserId,
            Long outboundId,
            OutboundCreateRequestDto request
    ) {
        OutboundResponseDto existingOutbound = outboundMapper.findById(
                companyId,
                outboundId
        );

        if (existingOutbound == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "존재하지 않는 출고서입니다."
            );
        }

        if (!"DRAFT".equals(existingOutbound.status())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 출고서만 수정할 수 있습니다."
            );
        }

        outboundValidator.validateUsableWarehouse(companyId, request.warehouseId());
        List<OutboundItemSaveDto> itemsToSave = prepareItemsToSave(companyId, request);

        if (outboundMapper.updateOutboundHeader(
                companyId,
                outboundId,
                request.salesOrderId(),
                request.warehouseId(),
                appUserId
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 출고서만 수정할 수 있습니다."
            );
        }

        outboundMapper.deleteOutboundItemLots(companyId, outboundId);
        outboundMapper.deleteOutboundItems(companyId, outboundId);
        saveOutboundItems(outboundId, itemsToSave, appUserId);

        return outboundMapper.findById(companyId, outboundId);
    }

    @Transactional(readOnly = true)
    public List<OutboundResponseDto> getOutboundList(Long companyId) {
        return outboundMapper.findAllByCompanyId(companyId);
    }

    @Transactional(readOnly = true)
    public List<OutboundWarehouseOptionDto> getWarehouseOptions(Long companyId) {
        return outboundMapper.findWarehouseOptions(companyId);
    }

    @Transactional(readOnly = true)
    public List<OutboundLotOptionDto> getLotOptions(
            Long companyId,
            Long warehouseId,
            Long productId
    ) {
        outboundValidator.validateUsableWarehouse(companyId, warehouseId);

        return outboundMapper.findLotOptions(
                companyId,
                warehouseId,
                productId
        );
    }

    @Transactional(readOnly = true)
    public OutboundDetailResponseDto getOutboundDetail(
            Long companyId,
            Long outboundId
    ) {
        OutboundResponseDto outbound = outboundMapper.findById(
                companyId,
                outboundId
        );

        if(outbound == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "존재하지 않는 출고서입니다."
            );
        }

        List<OutboundItemResponseDto> items =
                outboundMapper.findItemsByOutboundId(outboundId);

        List<OutboundItemLotResponseDto> lotAssignments =
                outboundMapper.findItemLotsByOutboundId(companyId, outboundId);

        return new OutboundDetailResponseDto(
                outbound,
                items,
                lotAssignments
        );
    }

    @Transactional
    public void deleteOutbound(Long companyId, Long outboundId) {
        OutboundResponseDto existingOutbound = outboundMapper.findById(
                companyId,
                outboundId
        );

        if (existingOutbound == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "존재하지 않는 출고서입니다."
            );
        }

        if (!"DRAFT".equals(existingOutbound.status())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "확정된 출고서는 삭제할 수 없습니다. 출고 취소를 사용해 주세요."
            );
        }

        outboundMapper.deleteOutboundItemLots(companyId, outboundId);
        outboundMapper.deleteOutboundItems(companyId, outboundId);

        if (outboundMapper.deleteOutbound(companyId, outboundId) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 출고서만 삭제할 수 있습니다."
            );
        }
    }

    @Transactional
    public OutboundResponseDto confirmOutbound(
            Long companyId,
            Long appUserId,
            Long outboundId
    ) {
        OutboundResponseDto outbound =
                outboundMapper.findById(companyId, outboundId);

        if(outbound == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "존재하지 않는 출고서입니다."
            );
        }

        if(!"DRAFT".equals(outbound.status())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 출고서만 확정할 수 있습니다."
            );
        }

        List<OutboundItemConfirmInfoDto> confirmItems =
                outboundMapper.findItemConfirmInfosByOutboundId(
                        companyId,
                        outboundId
                );

        if(confirmItems.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "출고 품목이 없는 출고서는 확정할 수 없습니다."
            );
        }

        List<OutboundLotConfirmInfoDto> lotConfirmInfos =
                outboundMapper.findLotConfirmInfosByOutboundId(
                        companyId,
                        outboundId
                );

        Map<Long, List<OutboundLotConfirmInfoDto>> lotsByOutboundItemId =
                groupLotsByOutboundItemId(lotConfirmInfos);

        // 조건부 상태 변경을 먼저 수행해 같은 출고서의 중복 확정을 막는다.
        int confirmedCount = outboundMapper.confirmOutbound(
                outboundId,
                companyId,
                appUserId
        );

        if(confirmedCount == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 출고서만 확정할 수 있습니다."
            );
        }

        for(OutboundItemConfirmInfoDto item : confirmItems) {
            int updatedCount =
                    outboundMapper.increaseSalesOrderItemShippedQty(
                            item.salesOrderItemId(),
                            item.baseShippedQty(),
                            item.version(),
                            appUserId
                    );

            if(updatedCount == 0) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                    "다른 출고 처리로 인해 수량이 변경되었습니다. 최신 주문 정보를 확인해 주세요."
                );
            }

            decreaseInventoryAndSaveHistory(
                    companyId,
                    outboundId,
                    appUserId,
                    item,
                    lotsByOutboundItemId.getOrDefault(
                            item.outboundItemId(),
                            List.of()
                    )
            );
        }

        outboundMapper.recalculateSalesOrderShipmentStatus(
                outbound.salesOrderId(),
                companyId,
                appUserId
        );

        return outboundMapper.findById(companyId, outboundId);

    }

    // 확정 출고를 취소하면서 판매주문·재고·재고이력을 모두 반대 방향으로 원복한다.
    @Transactional
    public OutboundResponseDto cancelOutbound(
            Long companyId,
            Long appUserId,
            Long outboundId,
            OutboundCancelRequestDto request
    ) {
        OutboundResponseDto outbound =
                outboundMapper.findById(companyId, outboundId);

        if (outbound == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "존재하지 않는 출고서입니다."
            );
        }

        if (!"CONFIRMED".equals(outbound.status())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "확정된 출고서만 취소할 수 있습니다."
            );
        }

        List<OutboundItemConfirmInfoDto> confirmItems =
                outboundMapper.findItemConfirmInfosByOutboundId(
                        companyId,
                        outboundId
                );

        if (confirmItems.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "출고 품목이 없는 출고서는 취소할 수 없습니다."
            );
        }

        if (outboundMapper.countOutboundStockHistory(
                companyId,
                outboundId
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "재고 차감 이력이 없는 이전 출고서는 자동 취소할 수 없습니다."
            );
        }

        List<OutboundLotConfirmInfoDto> lotConfirmInfos =
                outboundMapper.findLotConfirmInfosByOutboundId(
                        companyId,
                        outboundId
                );

        Map<Long, List<OutboundLotConfirmInfoDto>> lotsByOutboundItemId =
                groupLotsByOutboundItemId(lotConfirmInfos);

        // 상태를 먼저 조건부 변경해 동일 출고서를 두 번 취소하지 못하게 한다.
        if (outboundMapper.cancelOutbound(
                outboundId,
                companyId,
                appUserId,
                request.cancelReason()
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "확정된 출고서만 취소할 수 있습니다."
            );
        }

        for (OutboundItemConfirmInfoDto item : confirmItems) {
            if (outboundMapper.decreaseSalesOrderItemShippedQty(
                    item.salesOrderItemId(),
                    item.baseShippedQty(),
                    item.version(),
                    appUserId
            ) == 0) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "다른 출고 처리로 인해 주문 출고 수량이 변경되었습니다. 최신 주문 정보를 확인해 주세요."
                );
            }

            restoreInventoryAndSaveHistory(
                    companyId,
                    outboundId,
                    appUserId,
                    item,
                    lotsByOutboundItemId.getOrDefault(
                            item.outboundItemId(),
                            List.of()
                    )
            );
        }

        outboundMapper.recalculateSalesOrderShipmentStatus(
                outbound.salesOrderId(),
                companyId,
                appUserId
        );

        return outboundMapper.findById(companyId, outboundId);
    }

        private List<OutboundItemSaveDto> prepareItemsToSave(
                Long companyId,
                OutboundCreateRequestDto request
                ) {
            outboundValidator.validateNoDuplicateSalesOrderItemIds(
                    request.items()
            );

            List<OutboundItemSaveDto> itemsToSave = new ArrayList<>();

            for(OutboundItemCreateRequestDto item : request.items()) {
                OutboundItemOrderInfoDto orderInfo =
                        outboundValidator.getOutboundItemOrderInfo(
                                companyId,
                                request.salesOrderId(),
                                item
                        );
                itemsToSave.add(
                        outboundCalculator.calculateItem(item, orderInfo)
                );

                outboundValidator.validateLotAssignments(
                        companyId,
                        request.warehouseId(),
                        itemsToSave.get(itemsToSave.size() - 1)
                );
            }

            return itemsToSave;
        }

        private void saveOutboundItems(
                Long outboundId,
                List<OutboundItemSaveDto> itemsToSave,
                Long appUserId
                ) {
            for(int index = 0; index < itemsToSave.size(); index++) {
                outboundMapper.insertOutboundItem(
                        outboundId,
                        index +1,
                        itemsToSave.get(index),
                        appUserId
                );

                saveOutboundItemLots(itemsToSave.get(index), appUserId);
            }
        }

        private void saveOutboundItemLots(
                OutboundItemSaveDto item,
                Long appUserId
        ) {
            for (OutboundItemLotSaveDto lot : item.getLotAssignments()) {
                outboundMapper.insertOutboundItemLot(
                        item.getOutboundItemId(),
                        lot,
                        appUserId
                );
            }
        }

        private Map<Long, List<OutboundLotConfirmInfoDto>> groupLotsByOutboundItemId(
                List<OutboundLotConfirmInfoDto> lotConfirmInfos
        ) {
            Map<Long, List<OutboundLotConfirmInfoDto>> lotsByOutboundItemId =
                    new HashMap<>();

            for (OutboundLotConfirmInfoDto lotConfirmInfo : lotConfirmInfos) {
                lotsByOutboundItemId
                        .computeIfAbsent(
                                lotConfirmInfo.outboundItemId(),
                                ignored -> new ArrayList<>()
                        )
                        .add(lotConfirmInfo);
            }

            return lotsByOutboundItemId;
        }

        private void decreaseInventoryAndSaveHistory(
                Long companyId,
                Long outboundId,
                Long appUserId,
                OutboundItemConfirmInfoDto item,
                List<OutboundLotConfirmInfoDto> lotConfirmInfos
        ) {
            if ("Y".equals(item.lotManagedYn())) {
                validateLotAssignmentTotal(item, lotConfirmInfos);

                for (OutboundLotConfirmInfoDto lot : lotConfirmInfos) {
                    if (outboundMapper.decreaseLotStockQuantity(
                            companyId,
                            item.warehouseId(),
                            item.productId(),
                            lot.lotId(),
                            lot.baseLotQty()
                    ) == 0) {
                        throw new BusinessException(
                                ErrorCode.INVALID_REQUEST,
                                "LOT 재고가 부족하거나 다른 출고 처리로 수량이 변경되었습니다."
                        );
                    }

                    insertOutboundStockHistory(
                            companyId,
                            item.warehouseId(),
                            item.productId(),
                            lot.lotId(),
                            lot.baseLotQty().negate(),
                            outboundId,
                            appUserId
                    );
                }
            } else if (!lotConfirmInfos.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 비관리 상품에는 LOT 배정 정보가 있을 수 없습니다."
                );
            }

            if (outboundMapper.decreaseStockQuantity(
                    companyId,
                    item.warehouseId(),
                    item.productId(),
                    item.baseShippedQty()
            ) == 0) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "출고할 전체 재고가 부족하거나 다른 출고 처리로 수량이 변경되었습니다."
                );
            }

            if (!"Y".equals(item.lotManagedYn())) {
                insertOutboundStockHistory(
                        companyId,
                        item.warehouseId(),
                        item.productId(),
                        null,
                        item.baseShippedQty().negate(),
                        outboundId,
                        appUserId
                );
            }
        }

        private void validateLotAssignmentTotal(
                OutboundItemConfirmInfoDto item,
                List<OutboundLotConfirmInfoDto> lotConfirmInfos
        ) {
            BigDecimal totalLotQty = lotConfirmInfos.stream()
                    .map(OutboundLotConfirmInfoDto::baseLotQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalLotQty.compareTo(item.baseShippedQty()) != 0) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "출고 품목의 LOT 배정 수량 합계가 출고 수량과 일치하지 않습니다."
                );
            }
        }

        private void restoreInventoryAndSaveHistory(
                Long companyId,
                Long outboundId,
                Long appUserId,
                OutboundItemConfirmInfoDto item,
                List<OutboundLotConfirmInfoDto> lotConfirmInfos
        ) {
            if ("Y".equals(item.lotManagedYn())) {
                validateLotAssignmentTotal(item, lotConfirmInfos);

                for (OutboundLotConfirmInfoDto lot : lotConfirmInfos) {
                    if (outboundMapper.increaseLotStockQuantity(
                            companyId,
                            item.warehouseId(),
                            item.productId(),
                            lot.lotId(),
                            lot.baseLotQty()
                    ) == 0) {
                        throw new BusinessException(
                                ErrorCode.INVALID_REQUEST,
                                "원복할 LOT 재고를 찾을 수 없습니다."
                        );
                    }

                    insertOutboundCancelStockHistory(
                            companyId,
                            item.warehouseId(),
                            item.productId(),
                            lot.lotId(),
                            lot.baseLotQty(),
                            outboundId,
                            appUserId
                    );
                }
            } else if (!lotConfirmInfos.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "LOT 비관리 상품에는 LOT 배정 정보가 있을 수 없습니다."
                );
            }

            if (outboundMapper.increaseStockQuantity(
                    companyId,
                    item.warehouseId(),
                    item.productId(),
                    item.baseShippedQty()
            ) == 0) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "원복할 전체 재고를 찾을 수 없습니다."
                );
            }

            if (!"Y".equals(item.lotManagedYn())) {
                insertOutboundCancelStockHistory(
                        companyId,
                        item.warehouseId(),
                        item.productId(),
                        null,
                        item.baseShippedQty(),
                        outboundId,
                        appUserId
                );
            }
        }

        private void insertOutboundStockHistory(
                Long companyId,
                Long warehouseId,
                Long productId,
                Long lotId,
                BigDecimal changeQty,
                Long outboundId,
                Long appUserId
        ) {
            outboundMapper.insertOutboundStockHistory(
                    companyId,
                    warehouseId,
                    productId,
                    lotId,
                    changeQty,
                    outboundId,
                    appUserId
            );
        }

        private void insertOutboundCancelStockHistory(
                Long companyId,
                Long warehouseId,
                Long productId,
                Long lotId,
                BigDecimal changeQty,
                Long outboundId,
                Long appUserId
        ) {
            outboundMapper.insertOutboundCancelStockHistory(
                    companyId,
                    warehouseId,
                    productId,
                    lotId,
                    changeQty,
                    outboundId,
                    appUserId
            );
        }

        private OutboundSaveDto createOutboundSaveDto(
                Long companyId,
                Long appUserId,
                OutboundCreateRequestDto request
                ) {
            return new OutboundSaveDto(
                    companyId,
                    outboundNumberGenerator.createOutboundNo(),
                    request.salesOrderId(),
                    request.warehouseId(),
                    appUserId
            );
        }


    }

