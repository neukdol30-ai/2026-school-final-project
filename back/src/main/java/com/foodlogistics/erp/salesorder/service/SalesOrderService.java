package com.foodlogistics.erp.salesorder.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.salesorder.dto.SalesOrderCreateRequestDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderDetailResponseDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderItemCreateRequestDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderItemOrderInfoDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderItemResponseDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderItemSaveDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderResponseDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderSaveDto;
import com.foodlogistics.erp.salesorder.mapper.SalesOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private static final Long TEMPORARY_COMPANY_ID = 1L;
    private static final Long TEMPORARY_APP_USER_ID = 1L;

    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderValidator salesOrderValidator;
    private final SalesOrderCalculator salesOrderCalculator;
    private final SalesOrderNumberGenerator salesOrderNumberGenerator;

    public List<SalesOrderResponseDto> getSalesOrders() {
        return salesOrderMapper.findAll();
    }

    public SalesOrderDetailResponseDto getSalesOrderDetail(Long salesOrderId) {
        SalesOrderResponseDto salesOrder = salesOrderMapper.findById(salesOrderId);

        if (salesOrder == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        List<SalesOrderItemResponseDto> items =
                salesOrderMapper.findItemsBySalesOrderId(salesOrderId);

        return new SalesOrderDetailResponseDto(
                salesOrder.salesOrderId(),
                salesOrder.orderNo(),
                salesOrder.customerName(),
                salesOrder.orderStatus(),
                salesOrder.shipmentStatus(),
                items
        );
    }

    @Transactional
    public SalesOrderResponseDto createSalesOrder(
            SalesOrderCreateRequestDto request
    ) {
        salesOrderValidator.validateUsableCustomer(
                TEMPORARY_COMPANY_ID,
                request.customerId()
        );

        List<SalesOrderItemSaveDto> itemsToSave = prepareItemsToSave(request);
        SalesOrderAmountSummary amountSummary =
                salesOrderCalculator.summarize(itemsToSave);

        SalesOrderSaveDto salesOrderToSave = createSalesOrderSaveDto(
                request,
                amountSummary
        );

        salesOrderMapper.insertSalesOrder(salesOrderToSave);
        saveSalesOrderItems(salesOrderToSave.getSalesOrderId(), itemsToSave);

        SalesOrderResponseDto createdSalesOrder =
                salesOrderMapper.findById(salesOrderToSave.getSalesOrderId());

        log.info(
                "Sales order saved: salesOrderId={}, orderNo={}, itemCount={}",
                createdSalesOrder.salesOrderId(),
                createdSalesOrder.orderNo(),
                itemsToSave.size()
        );

        return createdSalesOrder;
    }

    // 검증이 끝난 화면 품목을 DB 저장용 품목 값으로 변환한다.
    private List<SalesOrderItemSaveDto> prepareItemsToSave(
            SalesOrderCreateRequestDto request
    ) {
        salesOrderValidator.validateNoDuplicateProductUnitIds(request.items());

        List<SalesOrderItemSaveDto> itemsToSave = new ArrayList<>();

        for (SalesOrderItemCreateRequestDto item : request.items()) {
            SalesOrderItemOrderInfoDto orderItemInfo =
                    salesOrderValidator.getUsableOrderItemInfo(
                            TEMPORARY_COMPANY_ID,
                            item.productUnitId()
                    );

            itemsToSave.add(salesOrderCalculator.calculateItem(item, orderItemInfo));
        }

        return itemsToSave;
    }

    // 헤더 저장 뒤 생성된 주문 ID를 사용해 각 품목을 순서대로 저장한다.
    private void saveSalesOrderItems(
            Long salesOrderId,
            List<SalesOrderItemSaveDto> itemsToSave
    ) {
        for (int index = 0; index < itemsToSave.size(); index++) {
            salesOrderMapper.insertSalesOrderItem(
                    salesOrderId,
                    index + 1,
                    itemsToSave.get(index),
                    TEMPORARY_APP_USER_ID
            );
        }
    }

    private SalesOrderSaveDto createSalesOrderSaveDto(
            SalesOrderCreateRequestDto request,
            SalesOrderAmountSummary amountSummary
    ) {
        return new SalesOrderSaveDto(
                TEMPORARY_COMPANY_ID,
                salesOrderNumberGenerator.generate(),
                request.customerId(),
                amountSummary.totalSupplyAmount(),
                amountSummary.totalTaxAmount(),
                amountSummary.totalAmount(),
                TEMPORARY_APP_USER_ID
        );
    }

    @Transactional
    public SalesOrderResponseDto confirmSalesOrder(Long salesOrderId) {
        int updatedCount = salesOrderMapper.confirmSalesOrder(
                salesOrderId,
                TEMPORARY_COMPANY_ID,
                TEMPORARY_APP_USER_ID
        );

        if (updatedCount == 0) {
            SalesOrderResponseDto existingSalesOrder =
                    salesOrderMapper.findById(salesOrderId);

            if (existingSalesOrder == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
            }

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 판매주문만 확정할 수 있습니다."
            );
        }

        SalesOrderResponseDto confirmedSalesOrder =
                salesOrderMapper.findById(salesOrderId);

        log.info(
                "Sales order confirmed: salesOrderId={}, orderNo={}",
                confirmedSalesOrder.salesOrderId(),
                confirmedSalesOrder.orderNo()
        );

        return confirmedSalesOrder;
    }
}