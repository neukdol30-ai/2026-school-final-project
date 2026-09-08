package com.foodlogistics.erp.outbound.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.outbound.dto.*;
import com.foodlogistics.erp.outbound.mapper.OutboundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboundService {

    private static final Long TEMPORARY_COMPANY_ID = 1L;
    private static final Long TEMPORARY_APP_USER_ID = 1L;

    private final OutboundMapper outboundMapper;
    private final OutboundValidator outboundValidator;
    private final OutboundCalculator outboundCalculator;
    private final OutboundNumberGenerator outboundNumberGenerator;

    @Transactional
    public OutboundResponseDto createOutbound(
            OutboundCreateRequestDto request
    ) {
        outboundValidator.validateUsableWarehouse(
                TEMPORARY_COMPANY_ID,
                request.warehouseId()
        );

        List<OutboundItemSaveDto> itemsToSave =
                prepareItemsToSave(request);

        OutboundSaveDto outboundToSave = createOutboundSaveDto(request);

        outboundMapper.insertOutbound(outboundToSave);

        saveOutboundItems(
                outboundToSave.getOutboundId(),
                itemsToSave
        );

        return outboundMapper.findById(outboundToSave.getOutboundId());

    }

    @Transactional
    public List<OutboundResponseDto> getOutboundList() {
        return  outboundMapper.findAllByCompanyId(TEMPORARY_COMPANY_ID);
    }
    @Transactional(readOnly = true)
    public OutboundDetailResponseDto getOutboundDetail(Long outboundId) {
        OutboundResponseDto outbound = outboundMapper.findById(outboundId);

        if(outbound == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "존재하지 않는 출고서입니다."
            );
        }

        List<OutboundItemResponseDto> items =
                outboundMapper.findItemsByOutboundId(outboundId);

        return new OutboundDetailResponseDto(
                outbound,
                items
        );
    }

    @Transactional
    public OutboundResponseDto confirmOutbound(Long outboundId) {
        OutboundResponseDto outbound =
                outboundMapper.findById(outboundId);

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
                outboundMapper.findItemConfirmInfosByOutboundId(outboundId);

        if(confirmItems.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "출고 품목이 없는 출고서는 확정할 수 없습니다."
            );
        }

        for(OutboundItemConfirmInfoDto item : confirmItems) {
            int updatedCount =
                    outboundMapper.increaseSalesOrderItemShippedQty(
                            item.salesOrderItemId(),
                            item.baseShippedQty(),
                            item.version(),
                            TEMPORARY_APP_USER_ID
                    );

            if(updatedCount == 0) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "다른 출고 처리로 인해 수량이 변경되었습니다. 최신 주문 정보를 확인해 주세요."
                );
            }
        }

        outboundMapper.recalculateSalesOrderShipmentStatus(
                outbound.salesOrderId(),
                TEMPORARY_COMPANY_ID,
                TEMPORARY_APP_USER_ID
        );

        int confirmedCount = outboundMapper.confirmOutbound(
                outboundId,
                TEMPORARY_COMPANY_ID,
                TEMPORARY_APP_USER_ID
        );

        if(confirmedCount == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "출고서를 확정할 수 없습니다."
            );
        }
        return outboundMapper.findById(outboundId);

    }

        private List<OutboundItemSaveDto> prepareItemsToSave(
                OutboundCreateRequestDto request
                ) {
            outboundValidator.validateNoDuplicateSalesOrderItemIds(
                    request.items()
            );

            List<OutboundItemSaveDto> itemsToSave = new ArrayList<>();

            for(OutboundItemCreateRequestDto item : request.items()) {
                OutboundItemOrderInfoDto orderInfo =
                        outboundValidator.getOutboundItemOrderInfo(
                                TEMPORARY_COMPANY_ID,
                                request.salesOrderId(),
                                item
                        );
                itemsToSave.add(
                        outboundCalculator.calculateItem(item, orderInfo)
                );
            }

            return itemsToSave;
        }

        private void saveOutboundItems(
                Long outboundId,
                List<OutboundItemSaveDto> itemsToSave
                ) {
            for(int index = 0; index < itemsToSave.size(); index++) {
                outboundMapper.insertOutboundItem(
                        outboundId,
                        index +1,
                        itemsToSave.get(index),
                        TEMPORARY_APP_USER_ID
                );
            }
        }

        private OutboundSaveDto createOutboundSaveDto(
                OutboundCreateRequestDto request
                ) {
            return new OutboundSaveDto(
                    TEMPORARY_COMPANY_ID,
                    outboundNumberGenerator.createOutboundNo(),
                    request.salesOrderId(),
                    request.warehouseId(),
                    TEMPORARY_APP_USER_ID
            );
        }


    }

