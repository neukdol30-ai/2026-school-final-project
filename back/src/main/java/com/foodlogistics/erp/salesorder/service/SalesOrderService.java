package com.foodlogistics.erp.salesorder.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.salesorder.dto.SalesOrderCreateRequestDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderDetailResponseDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderItemResponseDto;
import com.foodlogistics.erp.salesorder.dto.SalesOrderResponseDto;
import com.foodlogistics.erp.salesorder.mapper.SalesOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final SalesOrderMapper salesOrderMapper;

    private static final Long TEMPORARY_COMPANY_ID =1L;

    private static final Long TEMPORARY_APP_USER_ID = 1L;

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

    private final Map<Long,List<SalesOrderItemResponseDto>> salesOrderItems =
            new HashMap<>(
                    Map.of(
                            1L, List.of(
                                    new SalesOrderItemResponseDto(
                                            1L,
                                            new BigDecimal("3")
                                    )
                            ),
                            2L, List.of(
                                    new SalesOrderItemResponseDto(
                                            2L,
                                            new BigDecimal("1.5")
                                    )
                            )
                    )
            );

    private long nextSalesOrderId = 3L;

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

    public SalesOrderResponseDto createSalesOrder(
            SalesOrderCreateRequestDto request
    ) {

        Long salesOrderId = nextSalesOrderId;

        nextSalesOrderId++;

        String customerName = "임시 거래처" + request.customerId();

        SalesOrderResponseDto salesOrder = new SalesOrderResponseDto(
                salesOrderId,
                "SO-20260827-" + String.format("%03d", salesOrderId),
                customerName,

                "DRAFT",

                "NOT_SHIPPED"
        );
        salesOrders.add(salesOrder);

        List<SalesOrderItemResponseDto> createdItems =
                request.items().stream()
                                .map(item -> new SalesOrderItemResponseDto(
                                        item.productUnitId(),
                                        item.orderedQty()
                                ))
                                        .toList();

        salesOrderItems.put(salesOrderId,createdItems);

        log.info(
                "Sales order created: salesOrderId={} , orderNo={}, customerId ={}",
                salesOrder.salesOrderId(),
                salesOrder.orderNo(),
                request.customerId()
        );

        return salesOrder;
    }

    public SalesOrderResponseDto confirmSalesOrder(Long salesOrderId) {

        for (int index = 0; index < salesOrders.size(); index++) {
            SalesOrderResponseDto salesOrder = salesOrders.get(index);


            if (!salesOrder.salesOrderId().equals(salesOrderId)) {
                continue;
            }


            if ("CONFIRMED".equals(salesOrder.orderStatus())) {
                log.info(
                        "Sales order confirm skipped: salesOrderId={} is already confirmed",
                        salesOrderId
                );

                return salesOrder;
            }


            SalesOrderResponseDto confirmedSalesOrder =
                    new SalesOrderResponseDto(
                            salesOrder.salesOrderId(),
                            salesOrder.orderNo(),
                            salesOrder.customerName(),
                            "CONFIRMED",
                            salesOrder.shipmentStatus()
                    );

            // 목록에서 해당 주문 한 건만 확정된 주문으로 교체한다.
            salesOrders.set(index, confirmedSalesOrder);

            log.info(
                    "Sales order confirmed: salesOrderId={}, orderNo={}",
                    confirmedSalesOrder.salesOrderId(),
                    confirmedSalesOrder.orderNo()
            );

            return confirmedSalesOrder;
        }


        log.warn(
                "Sales order confirm failed: salesOrderId={} not found",
                salesOrderId
        );

        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }
}