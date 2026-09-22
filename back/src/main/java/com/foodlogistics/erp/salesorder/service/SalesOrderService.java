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

    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderValidator salesOrderValidator;
    private final SalesOrderCalculator salesOrderCalculator;
    private final SalesOrderNumberGenerator salesOrderNumberGenerator;

    public List<SalesOrderResponseDto> getSalesOrders(Long companyId) {
        return salesOrderMapper.findAllByCompanyId(companyId);
    }

    public SalesOrderDetailResponseDto getSalesOrderDetail(
            Long companyId,
            Long salesOrderId
    ) {
        SalesOrderResponseDto salesOrder = salesOrderMapper.findById(
                companyId,
                salesOrderId
        );

        if (salesOrder == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        List<SalesOrderItemResponseDto> items =
                salesOrderMapper.findItemsBySalesOrderId(companyId, salesOrderId);

        return new SalesOrderDetailResponseDto(
                salesOrder.salesOrderId(),
                salesOrder.orderNo(),
                salesOrder.customerId(),
                salesOrder.customerName(),
                salesOrder.orderStatus(),
                salesOrder.shipmentStatus(),
                items
        );
    }

    // 초안 판매주문은 헤더와 품목을 모두 다시 계산해 교체한다.
    @Transactional
    public SalesOrderResponseDto updateSalesOrder(
            Long companyId,
            Long appUserId,
            Long salesOrderId,
            SalesOrderCreateRequestDto request
    ) {
        SalesOrderResponseDto existingSalesOrder = salesOrderMapper.findById(
                companyId,
                salesOrderId
        );

        if (existingSalesOrder == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        if (!"DRAFT".equals(existingSalesOrder.orderStatus())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 판매주문만 수정할 수 있습니다."
            );
        }

        salesOrderValidator.validateUsableCustomer(companyId, request.customerId());

        List<SalesOrderItemSaveDto> itemsToSave = prepareItemsToSave(
                companyId,
                request
        );
        SalesOrderAmountSummary amountSummary = salesOrderCalculator.summarize(itemsToSave);
        SalesOrderSaveDto salesOrderToSave = new SalesOrderSaveDto(
                companyId,
                existingSalesOrder.orderNo(),
                request.customerId(),
                amountSummary.totalSupplyAmount(),
                amountSummary.totalTaxAmount(),
                amountSummary.totalAmount(),
                appUserId
        );
        salesOrderToSave.setSalesOrderId(salesOrderId);

        if (salesOrderMapper.updateSalesOrder(salesOrderToSave, appUserId) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 판매주문만 수정할 수 있습니다."
            );
        }

        salesOrderMapper.deleteSalesOrderItems(companyId, salesOrderId);
        saveSalesOrderItems(salesOrderId, itemsToSave, appUserId);

        return salesOrderMapper.findById(companyId, salesOrderId);
    }

    @Transactional
    public SalesOrderResponseDto createSalesOrder(
            Long companyId,
            Long appUserId,
            SalesOrderCreateRequestDto request
    ) {
        salesOrderValidator.validateUsableCustomer(
                companyId,
                request.customerId()
        );

        List<SalesOrderItemSaveDto> itemsToSave = prepareItemsToSave(
                companyId,
                request
        );
        SalesOrderAmountSummary amountSummary =
                salesOrderCalculator.summarize(itemsToSave);

        SalesOrderSaveDto salesOrderToSave = createSalesOrderSaveDto(
                request,
                amountSummary,
                companyId,
                appUserId
        );

        salesOrderMapper.insertSalesOrder(salesOrderToSave);
        saveSalesOrderItems(
                salesOrderToSave.getSalesOrderId(),
                itemsToSave,
                appUserId
        );

        SalesOrderResponseDto createdSalesOrder = salesOrderMapper.findById(
                companyId,
                salesOrderToSave.getSalesOrderId()
        );

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
            Long companyId,
            SalesOrderCreateRequestDto request
    ) {
        salesOrderValidator.validateNoDuplicateProductUnitIds(request.items());

        List<SalesOrderItemSaveDto> itemsToSave = new ArrayList<>();

        for (SalesOrderItemCreateRequestDto item : request.items()) {
            SalesOrderItemOrderInfoDto orderItemInfo =
                    salesOrderValidator.getUsableOrderItemInfo(
                            companyId,
                            item.productUnitId()
                    );

            itemsToSave.add(salesOrderCalculator.calculateItem(item, orderItemInfo));
        }

        return itemsToSave;
    }

    // 헤더 저장 뒤 생성된 주문 ID를 사용해 각 품목을 순서대로 저장한다.
    private void saveSalesOrderItems(
            Long salesOrderId,
            List<SalesOrderItemSaveDto> itemsToSave,
            Long appUserId
    ) {
        for (int index = 0; index < itemsToSave.size(); index++) {
            salesOrderMapper.insertSalesOrderItem(
                    salesOrderId,
                    index + 1,
                    itemsToSave.get(index),
                    appUserId
            );
        }
    }

    private SalesOrderSaveDto createSalesOrderSaveDto(
            SalesOrderCreateRequestDto request,
            SalesOrderAmountSummary amountSummary,
            Long companyId,
            Long appUserId
    ) {
        return new SalesOrderSaveDto(
                companyId,
                salesOrderNumberGenerator.generate(),
                request.customerId(),
                amountSummary.totalSupplyAmount(),
                amountSummary.totalTaxAmount(),
                amountSummary.totalAmount(),
                appUserId
        );
    }

    @Transactional
    public SalesOrderResponseDto confirmSalesOrder(
            Long companyId,
            Long appUserId,
            Long salesOrderId
    ) {
        int updatedCount = salesOrderMapper.confirmSalesOrder(
                salesOrderId,
                companyId,
                appUserId
        );

        if (updatedCount == 0) {
            SalesOrderResponseDto existingSalesOrder =
                    salesOrderMapper.findById(companyId, salesOrderId);

            if (existingSalesOrder == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
            }

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "작성중인 판매주문만 확정할 수 있습니다."
            );
        }

        SalesOrderResponseDto confirmedSalesOrder =
                salesOrderMapper.findById(companyId, salesOrderId);

        log.info(
                "Sales order confirmed: salesOrderId={}, orderNo={}",
                confirmedSalesOrder.salesOrderId(),
                confirmedSalesOrder.orderNo()
        );

        return confirmedSalesOrder;
    }

    @Transactional
    public SalesOrderResponseDto cancelSalesOrder(
            Long companyId,
            Long appUserId,
            Long salesOrderId
    ) {
        SalesOrderResponseDto existingSalesOrder = salesOrderMapper.findById(
                companyId,
                salesOrderId
        );

        if (existingSalesOrder == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        if (salesOrderMapper.cancelSalesOrder(
                companyId,
                salesOrderId,
                appUserId
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "미출고이며 진행 중인 출고서가 없는 판매주문만 취소할 수 있습니다."
            );
        }

        return salesOrderMapper.findById(companyId, salesOrderId);
    }
}
