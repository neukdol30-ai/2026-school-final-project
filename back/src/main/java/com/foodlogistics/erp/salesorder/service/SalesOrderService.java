package com.foodlogistics.erp.salesorder.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.salesorder.dto.*;
import com.foodlogistics.erp.salesorder.mapper.SalesOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final SalesOrderMapper salesOrderMapper;

    private static final Long TEMPORARY_COMPANY_ID =1L;

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private static final Long TEMPORARY_APP_USER_ID = 1L;


    public List<SalesOrderResponseDto> getSalesOrders() {

        return salesOrderMapper.findAll();
    }

    public SalesOrderDetailResponseDto getSalesOrderDetail(Long salesOrderId) {
        SalesOrderResponseDto salesOrder =
                salesOrderMapper.findById(salesOrderId);

        if(salesOrder == null) {
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

        List<SalesOrderItemSaveDto> itemsToSave =
                validateAndCalculateSalesOrderItems(request);

        SalesOrderSaveDto salesOrderToSave =
                createSalesOrderSaveDto(request, itemsToSave);

        salesOrderMapper.insertSalesOrder(salesOrderToSave);

        for(int index = 0; index < itemsToSave.size(); index++) {
            SalesOrderItemSaveDto itemToSave = itemsToSave.get(index);

            salesOrderMapper.insertSalesOrderItem(
                    salesOrderToSave.getSalesOrderId(),
                    index + 1,
                    itemToSave,
                    TEMPORARY_APP_USER_ID
            );
        }

        SalesOrderResponseDto createdSalesOrder =
                salesOrderMapper.findById(
                        salesOrderToSave.getSalesOrderId()
                );

        log.info(
               "Sales order saved: salesOrederId={}, orderNo={}, itemCount={}",
                createdSalesOrder.salesOrderId(),
                createdSalesOrder.orderNo(),
                itemsToSave.size()
        );

        return createdSalesOrder;
    }

   private List<SalesOrderItemSaveDto> validateAndCalculateSalesOrderItems(
           SalesOrderCreateRequestDto request
   ) {
        if(salesOrderMapper.countUsableCustomer(
                TEMPORARY_COMPANY_ID,
                request.customerId()
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "주문 가능한 고객 거래처가 아닙니다."
            );
        }

        if(salesOrderMapper.countUsableWarehouse(
                TEMPORARY_COMPANY_ID,
                request.warehouseId()
        ) == 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "사용 가능한 출고 창고가 아닙니다."
            );
        }

        List<SalesOrderItemSaveDto> itemsToSave = new ArrayList<>();

        for(SalesOrderItemCreateRequestDto item : request.items()) {
            SalesOrderItemOrderInfoDto orderItemInfo =
                    salesOrderMapper.findOrderItemInfoByProductUnitId(
                            item.productUnitId()
                    );

            if(orderItemInfo == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "사용 가능한 상품 단위가 아닙니다."
                );
            }

            if(!TEMPORARY_COMPANY_ID.equals(orderItemInfo.companyId())) {
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "현재 회사의 상품이 아닙니다."
                );
            }

            itemsToSave.add(
                    calculateSalesOrderItem(item,orderItemInfo)
            );
        }
        return itemsToSave;
   }

    private SalesOrderItemSaveDto calculateSalesOrderItem(

            SalesOrderItemCreateRequestDto item,

            SalesOrderItemOrderInfoDto orderItemInfo
    )  {
        BigDecimal baseOrderedQty = item.orderedQty()
                .multiply(orderItemInfo.conversionQty());

        BigDecimal supplyAmount = item.unitPrice()
                .multiply(item.orderedQty())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxAmount = "TAXABLE".equals(orderItemInfo.taxType())
                ? supplyAmount.multiply(VAT_RATE)
                  .setScale(2,RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalAmount = supplyAmount.add(taxAmount);

        return new SalesOrderItemSaveDto(
                orderItemInfo.productId(),
                item.productUnitId(),
                item.orderedQty(),
                orderItemInfo.conversionQty(),
                baseOrderedQty,
                item.unitPrice(),
                orderItemInfo.taxType(),
                supplyAmount,
                taxAmount,
                totalAmount
        );
    }

    private SalesOrderSaveDto createSalesOrderSaveDto(

            SalesOrderCreateRequestDto request,

            List<SalesOrderItemSaveDto> itemsToSave
    ) {
        BigDecimal totalSupplyAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SalesOrderItemSaveDto itemToSave : itemsToSave) {
            totalSupplyAmount = totalSupplyAmount.add(
                    itemToSave.supplyAmount()
            );

            totalTaxAmount = totalTaxAmount.add(
                    itemToSave.taxAmount()
            );

            totalAmount = totalAmount.add(
                    itemToSave.totalAmount()
            );

        }

        return new SalesOrderSaveDto(
                TEMPORARY_COMPANY_ID,
                createOrderNo(),
                request.customerId(),
                request.warehouseId(),
                totalSupplyAmount,
                totalTaxAmount,
                totalAmount,
                TEMPORARY_APP_USER_ID

        );
    }

    private String createOrderNo() {
        String orderDate = LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        String randomCode = UUID.randomUUID()
                .toString()
                .substring(0,8)
                .toUpperCase();

        return "SO-" + orderDate + "-" + randomCode;
    }

    @Transactional
    public SalesOrderResponseDto confirmSalesOrder(Long salesOrderId) {

        int updatedCount = salesOrderMapper.confirmSalesOrder(
                salesOrderId,
                TEMPORARY_COMPANY_ID,
                TEMPORARY_APP_USER_ID
        );

        if(updatedCount == 0) {
            SalesOrderResponseDto existingSalesOrder =
                    salesOrderMapper.findById(salesOrderId);

            if( existingSalesOrder == null) {
                throw new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND
                );
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

