package com.foodlogistics.erp.purchase.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Oracle의 PURCHASE_ORDER 테이블 한 행을 Java 객체 하나로 연결하는 JPA entity.
// 기존 발주 등록, 조회, 수정은 계속 MyBatis. 승인 요청에서 이 Entity를 JPA로 조회.
@Entity
@Table(name = "purchase_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_id")
    private Long purchaseOrderId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private PurchaseOrderApprovalStatus approvalStatus;

    @Column(name = "receipt_status", nullable = false, length = 20)
    private String receiptStatus;

    @Column(name = "request_note", length = 500)
    private String requestNote;

    @Column(name = "internal_memo", length = 500)
    private String internalMemo;

    @Column(name = "total_supply_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalSupplyAmount;

    @Column(name = "total_tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalTaxAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public void requestApproval(Long appUserId) {
        this.approvalStatus = PurchaseOrderApprovalStatus.PENDING;
        this.updatedBy = appUserId;
        this.updatedAt = LocalDateTime.now();
    }
}
