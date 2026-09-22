package com.foodlogistics.erp.unit;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.unit.dto.MeasurementUnitResponse;
import com.foodlogistics.erp.unit.dto.MeasurementUnitSaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeasurementUnitService {

    private final MeasurementUnitMapper measurementUnitMapper;

    public List<MeasurementUnitResponse> getUnits(
            String keyword,
            String useYn
    ) {
        return measurementUnitMapper.findAll(
                        trimToNull(keyword),
                        normalizeUseYn(useYn)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public MeasurementUnitResponse getUnit(Long unitId) {
        return toResponse(
                findUnit(unitId)
        );
    }

    @Transactional
    public MeasurementUnitResponse createUnit(
            MeasurementUnitSaveRequest request
    ) {
        String unitCode =
                normalizeUnitCode(
                        request.getUnitCode()
                );

        validateDuplicateCode(
                unitCode,
                null
        );

        MeasurementUnit measurementUnit =
                new MeasurementUnit();

        measurementUnit.setUnitCode(unitCode);
        measurementUnit.setUseYn("Y");

        applyRequest(
                measurementUnit,
                request
        );

        measurementUnitMapper.insert(
                measurementUnit
        );

        return getUnit(
                measurementUnit.getUnitId()
        );
    }

    @Transactional
    public MeasurementUnitResponse updateUnit(
            Long unitId,
            MeasurementUnitSaveRequest request
    ) {
        findUnit(unitId);

        String unitCode =
                normalizeUnitCode(
                        request.getUnitCode()
                );

        validateDuplicateCode(
                unitCode,
                unitId
        );

        MeasurementUnit measurementUnit =
                new MeasurementUnit();

        measurementUnit.setUnitId(unitId);
        measurementUnit.setUnitCode(unitCode);

        applyRequest(
                measurementUnit,
                request
        );

        int updatedCount =
                measurementUnitMapper.update(
                        measurementUnit
                );

        if (updatedCount == 0) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }

        return getUnit(unitId);
    }

    @Transactional
    public void deactivateUnit(Long unitId) {
        findUnit(unitId);

        int updatedCount =
                measurementUnitMapper.updateUseYn(
                        unitId,
                        "N"
                );

        if (updatedCount == 0) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }
    }

    private MeasurementUnit findUnit(Long unitId) {
        MeasurementUnit measurementUnit =
                measurementUnitMapper.findById(unitId);

        if (measurementUnit == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }

        return measurementUnit;
    }

    private void validateDuplicateCode(
            String unitCode,
            Long excludeUnitId
    ) {
        int duplicateCount =
                measurementUnitMapper.countByUnitCode(
                        unitCode,
                        excludeUnitId
                );

        if (duplicateCount > 0) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_UNIT_CODE
            );
        }
    }

    private void applyRequest(
            MeasurementUnit measurementUnit,
            MeasurementUnitSaveRequest request
    ) {
        measurementUnit.setUnitName(
                request.getUnitName().trim()
        );

        measurementUnit.setDescription(
                trimToNull(request.getDescription())
        );
    }

    private String normalizeUnitCode(String unitCode) {
        return unitCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeUseYn(String useYn) {
        String normalized = trimToNull(useYn);

        if (normalized == null
                || "ALL".equalsIgnoreCase(normalized)) {
            return null;
        }

        normalized = normalized.toUpperCase(Locale.ROOT);

        if (!"Y".equals(normalized)
                && !"N".equals(normalized)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private MeasurementUnitResponse toResponse(
            MeasurementUnit measurementUnit
    ) {
        return new MeasurementUnitResponse(
                measurementUnit.getUnitId(),
                measurementUnit.getUnitCode(),
                measurementUnit.getUnitName(),
                measurementUnit.getDescription(),
                measurementUnit.getUseYn(),
                measurementUnit.getCreatedAt(),
                measurementUnit.getUpdatedAt()
        );
    }
}