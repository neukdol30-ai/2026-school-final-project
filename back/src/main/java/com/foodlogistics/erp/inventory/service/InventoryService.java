package com.foodlogistics.erp.inventory.service;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.inventory.dto.InventoryStockResponse;
import com.foodlogistics.erp.inventory.dto.InventoryLotResponse;
import com.foodlogistics.erp.inventory.dto.InventoryHistoryResponse;
import com.foodlogistics.erp.inventory.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

// 조회 전용 트랜잭션을 사용한다. 기존 입고·출고·실사의 수량 변경 Service는 호출하지 않는다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private static final ZoneId KOREA_TIME_ZONE = ZoneId.of("Asia/Seoul");

    // V25의 제약조건에 정의된 값만 허용한다. 임의의 새로운 재고 유형을 만들지 않는다.
    // INBOUND_CANCEL은 DB에서 허용하지만 현재 입고 취소 코드가 생성하는 이력은 아니다.
    private static final Set<String> MOVEMENT_TYPES = Set.of(
            "INBOUND", "OUTBOUND", "OUTBOUND_CANCEL", "ADJUSTMENT", "INBOUND_CANCEL"
    );

    private final InventoryMapper inventoryMapper;

    public List<InventoryStockResponse> getStocks(
            Long companyId, String warehouseKeyword, String productKeyword, String includeZero
    ) {
        validateCompanyId(companyId);
        return inventoryMapper.findStocks(
                companyId, normalizeKeyword(warehouseKeyword), normalizeKeyword(productKeyword),
                parseIncludeZero(includeZero)
        );
    }

    public List<InventoryLotResponse> getLots(
            Long companyId, String warehouseKeyword, String productKeyword,
            String lotNo, String includeZero
    ) {
        validateCompanyId(companyId);
        return inventoryMapper.findLots(
                companyId, normalizeKeyword(warehouseKeyword), normalizeKeyword(productKeyword),
                normalizeKeyword(lotNo), parseIncludeZero(includeZero)
        );
    }

    public List<InventoryHistoryResponse> getHistory(
            Long companyId, String warehouseKeyword, String productKeyword,
            String lotNo, String movementType, String startDate, String endDate
    ) {
        validateCompanyId(companyId);

        // 서버의 시스템 시간대와 무관하게 한국의 오늘 날짜에서 한 달을 뺀다.
        // 날짜를 생략한 직접 API 호출도 전체 기간 조회로 바뀌지 않는다.
        LocalDate today = LocalDate.now(KOREA_TIME_ZONE);
        LocalDate from = parseDate(startDate, today.minusMonths(1));
        LocalDate to = parseDate(endDate, today);

        // 기존 발주 검색처럼 시작일이 종료일보다 늦으면 INVALID_REQUEST를 사용한다.
        if (from.isAfter(to)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST, "조회 시작일은 종료일보다 늦을 수 없습니다."
            );
        }

        String normalizedMovementType = trimToNull(movementType);
        if (normalizedMovementType != null && !MOVEMENT_TYPES.contains(normalizedMovementType)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST, "올바른 변동유형을 선택해 주세요."
            );
        }

        // created_at은 한국시간 TIMESTAMP이다. 날짜의 시작 시각을 그대로 바인딩한다.
        // 종료일 + 1일의 0시 미만으로 비교하여 종료일의 소수 초까지 빠짐없이 포함한다.
        return inventoryMapper.findHistory(
                companyId, normalizeKeyword(warehouseKeyword), normalizeKeyword(productKeyword),
                normalizeKeyword(lotNo), normalizedMovementType,
                from.atStartOfDay(), to.plusDays(1).atStartOfDay()
        );
    }

    private void validateCompanyId(Long companyId) {
        if (companyId == null || companyId <= 0) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
    }

    // 잘못된 체크박스 값을 묵인하지 않고 기존 공통 오류 형식으로 전달한다.
    private boolean parseIncludeZero(String value) {
        if (value == null || "false".equals(value)) {
            return false;
        }
        if ("true".equals(value)) {
            return true;
        }
        throw new BusinessException(ErrorCode.INVALID_REQUEST, "재고 0 포함 여부를 확인해 주세요.");
    }

    private LocalDate parseDate(String value, LocalDate defaultDate) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return defaultDate;
        }

        // 화면과 같은 네 자리 연도만 받고, Oracle 범위 밖 날짜를 전송하지 않는다.
        try {
            if (!normalized.matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new DateTimeParseException("Invalid date format", normalized, 0);
            }
            LocalDate date = LocalDate.parse(normalized);
            if (date.getYear() < 1 || date.equals(LocalDate.of(9999, 12, 31))) {
                throw new DateTimeParseException("Date out of range", normalized, 0);
            }
            return date;
        } catch (DateTimeParseException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST, "날짜는 올바른 연도-월-일 형식으로 입력해 주세요."
            );
        }
    }

    // SQL 문장에 검색어를 붙이지 않는다. Mapper의 바인딩 값으로만 전달한다.
    private String normalizeKeyword(String value) {
        String normalized = trimToNull(value);
        if (normalized != null && normalized.length() > 100) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "검색어는 100자 이하여야 합니다.");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
