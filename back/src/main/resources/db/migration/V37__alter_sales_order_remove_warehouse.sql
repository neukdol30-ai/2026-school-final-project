-- 판매주문은 여러 실제 출고 창고로 나뉠 수 있다.
-- 출고 창고는 OUTBOUND.warehouse_id에서만 관리한다.
ALTER TABLE sales_order
DROP CONSTRAINT fk_sales_order_warehouse;

ALTER TABLE sales_order
DROP COLUMN warehouse_id;
