package com.foodlogistics.erp.salesorder.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.salesorder.dto.SalesOrderCreateRequestDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderResponseDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SalesOrderService {

    private final List<SalesOrderResponseDto> salesOrders = new ArrayList<>(

        List.of(
                new SalesOrderResponseDto(
                        1L,
                        "SO-20260827-001",
                        "한빛식당",
                        "CONFIRMED",
                        "NOT_SHIPPED"
                ),

                new SalesOrderResponseDto(
                        2L,
                        "SO-20260827-002",
                        "푸른마켓",
                        "DRAFT",
                        "NOT_SHIPPED"
                )
        )
    );

    private long nextSalesOrderId = 3L;

    public List<SalesOrderResponseDto> getSalesOrders() {
        return salesOrders;
    }

    public SalesOrderResponseDto createSalesOrder(
            SalesOrderCreateRequestDto request
    ) {

        Long salesOrderId = nextSalesOrderId;

        nextSalesOrderId++;

        String customerName = "임시 거래처"  + request.customerId();

        SalesOrderResponseDto salesOrder = new SalesOrderResponseDto(
                salesOrderId,
                "SO-20260827-" +String.format("%03d", salesOrderId),
                customerName,

                "DRAFT",

                "NOT_SHIPPED"
        );
        salesOrders.add(salesOrder);

        return salesOrder;
}

    public SalesOrderResponseDto confirmSalesOrder(Long salesOrderId) {

        for(int index = 0; index < salesOrders.size(); index++) {
            SalesOrderResponseDto salesOrder = salesOrders.get(index);

            if(salesOrder.salesOrderId().equals(salesOrderId)) {

                if("CONFIRMED".equals(salesOrder.orderStatus())) {
                    return  salesOrder;
                }

                SalesOrderResponseDto confirmedSalesOrder =
                        new SalesOrderResponseDto(
                                salesOrder.salesOrderId(),
                                salesOrder.orderNo(),
                                salesOrder.customerName(),
                                "CONFIRMED",
                                salesOrder.shipmentStatus()
                        );

                salesOrders.set(index , confirmedSalesOrder);

                return confirmedSalesOrder;
            }
        }
        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }

}
