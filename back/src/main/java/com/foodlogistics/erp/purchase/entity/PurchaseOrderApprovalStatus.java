package com.foodlogistics.erp.purchase.entity;

// Oracle PURCHASE_ORDER.APPROVAL_STATUS에 들어갈 수 있는 네 상태를 Java에서도 정해두는 파일.
public enum PurchaseOrderApprovalStatus {

    DRAFT,
    PENDING,
    APPROVED,
    REJECTED
}
