package com.foodlogistics.erp.purchase.repository;

import com.foodlogistics.erp.purchase.entity.PurchaseOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PurchaseOrder> findByPurchaseOrderIdAndCompanyId(
            Long purchaseOrderId,
            Long companyId
    );
}
