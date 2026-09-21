package com.foodlogistics.erp.purchase.service;

import com.foodlogistics.erp.purchase.dto.PurchaseOrderCreateRequest;
import com.foodlogistics.erp.purchase.dto.PurchaseOrderItemCreateRequest;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderInsertParam;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderItemInsertParam;
import com.foodlogistics.erp.purchase.mapper.PurchaseOrderMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("security-test")
class PurchaseOrderTransactionIntegrationTest {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private PurchaseOrderMapper purchaseOrderMapper;

    @Test
    @DisplayName("발주 품목 저장 실패 시 발주 Header까지 Rollback되어야 한다")
    void createPurchaseOrder_whenItemInsertFails_rollsBackHeader() {
        Long companyId =
                jdbcTemplate.queryForObject(
                        """
                                SELECT company_id
                                FROM business_partner
                                WHERE supplier_yn = 'Y'
                                AND use_yn = 'Y'
                                FETCH FIRST 1 ROWS ONLY
                                """,
                        Long.class
                );

        Long supplierId =
                jdbcTemplate.queryForObject(
                        """
                                SELECT partner_id
                                FROM business_partner
                                WHERE company_id = ?
                                AND supplier_yn = 'Y'
                                AND use_yn = 'Y'
                                FETCH FIRST 1 ROWS ONLY
                                """,
                        Long.class,
                        companyId
                );

        Long warehouseId =
                jdbcTemplate.queryForObject(
                        """
                                SELECT warehouse_id
                                FROM warehouse
                                WHERE company_id = ?
                                AND use_yn = 'Y'
                                FETCH FIRST 1 ROWS ONLY
                                """,
                        Long.class,
                        companyId
                );

        Long productId =
                jdbcTemplate.queryForObject(
                        """
                                SELECT product_id
                                FROM product
                                WHERE company_id = ?
                                AND use_yn = 'Y'
                                FETCH FIRST 1 ROWS ONLY
                                """,
                        Long.class,
                        companyId
                );

        Long productUnitId =
                jdbcTemplate.queryForObject(
                        """
                                SELECT pu.product_unit_id
                                FROM product_unit pu
                                INNER JOIN unit u
                                ON u.unit_id = pu.unit_id
                                WHERE pu.product_id = ?
                                AND pu.use_yn = 'Y'
                                AND u.use_yn = 'Y'
                                FETCH FIRST 1 ROWS ONLY
                                """,
                        Long.class,
                        productId
                );

        Long appUserId =
                jdbcTemplate.queryForObject(
                        """
                                SELECT app_user_id
                                FROM app_user
                                WHERE company_id = ?
                                FETCH FIRST 1 ROWS ONLY
                                """,
                        Long.class,
                        companyId
                );

        PurchaseOrderItemCreateRequest item = new PurchaseOrderItemCreateRequest();

        item.setProductId(productId);
        item.setProductUnitId(productUnitId);
        item.setOrderedQty(
                new BigDecimal("1")
        );
        item.setUnitPrice(
                new BigDecimal("1200")
        );

        PurchaseOrderCreateRequest request =
                new PurchaseOrderCreateRequest();

        request.setSupplierId(supplierId);
        request.setWarehouseId(warehouseId);
        request.setOrderDate(
                LocalDate.of(2026, 9, 14)
        );
        request.setExpectedDeliveryDate(
                LocalDate.of(2026, 9, 15)
        );
        request.setRequestNote(
                "TRANSACTION ROLLBACK TEST"
        );
        request.setInternalMemo(
                "강제 Rollback 통합테스트"
        );
        request.setItems(
                List.of(item)
        );

        doReturn(0)
                .when(purchaseOrderMapper)
                .insertPurchaseOrderItem(
                        any(PurchaseOrderItemInsertParam.class)
                );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> purchaseOrderService.createPurchaseOrder(
                                companyId,
                                appUserId,
                                request
                        )
                );

        assertEquals(
                "발주 품목 저장에 실패했습니다.",
                exception.getMessage()
        );

        ArgumentCaptor<PurchaseOrderInsertParam> headerCaptor =
                ArgumentCaptor.forClass(
                        PurchaseOrderInsertParam.class
                );

        verify(purchaseOrderMapper)
                .insertPurchaseOrder(
                        headerCaptor.capture()
                );

        PurchaseOrderInsertParam insertedHeader =
                headerCaptor.getValue();

        assertNotNull(
                insertedHeader.getPurchaseOrderId()
        );

        verify(purchaseOrderMapper)
                .insertPurchaseOrderItem(
                        any(PurchaseOrderItemInsertParam.class)
                );

        String orderNo =
                insertedHeader.getOrderNo();

        Long purchaseOrderId =
                insertedHeader.getPurchaseOrderId();

        Integer headerCount =
                jdbcTemplate.queryForObject(
                        """
                                SELECT COUNT(*)
                                FROM purchase_order
                                WHERE order_no = ?
                                """,
                        Integer.class,
                        orderNo
                );

        Integer itemCount =
                jdbcTemplate.queryForObject(
                        """
                                SELECT COUNT(*)
                                FROM purchase_order_item
                                WHERE purchase_order_id = ?
                                """,
                        Integer.class,
                        purchaseOrderId
                );

        assertEquals(
                0,
                headerCount
        );

        assertEquals(
                0,
                itemCount
        );

        System.out.println(
                "Rollback 테스트 발주번호 = " + orderNo
        );

        System.out.println(
                "Rollback 후 PURCHASE_ORDER 건수 = " + headerCount
        );

        System.out.println(
                "Rollback 후 PURCHASE_ORDER_ITEM 건수 = " + itemCount
        );
    }
}