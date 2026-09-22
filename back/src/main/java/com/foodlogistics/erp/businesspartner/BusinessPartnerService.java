package com.foodlogistics.erp.businesspartner;

import com.foodlogistics.erp.businesspartner.dto.BusinessPartnerResponse;
import com.foodlogistics.erp.businesspartner.dto.BusinessPartnerSaveRequest;
import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessPartnerService {

    private final BusinessPartnerMapper businessPartnerMapper;

    public List<BusinessPartnerResponse> getPartners(
            Long companyId,
            String keyword,
            String partnerType,
            String useYn
    ) {
        return businessPartnerMapper.findAll(
                        companyId,
                        trimToNull(keyword),
                        normalizePartnerType(partnerType),
                        normalizeUseYn(useYn)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BusinessPartnerResponse getPartner(
            Long companyId,
            Long partnerId
    ) {
        return toResponse(
                findPartner(companyId, partnerId)
        );
    }

    @Transactional
    public BusinessPartnerResponse createPartner(
            Long companyId,
            Long appUserId,
            BusinessPartnerSaveRequest request
    ) {
        validatePartnerRole(request);

        String partnerCode =
                normalizePartnerCode(
                        request.getPartnerCode()
                );

        String businessNumber =
                normalizeBusinessNumber(
                        request.getBusinessNumber()
                );

        validateDuplicate(
                companyId,
                partnerCode,
                businessNumber,
                null
        );

        BusinessPartner businessPartner =
                new BusinessPartner();

        businessPartner.setCompanyId(companyId);
        businessPartner.setPartnerCode(partnerCode);
        businessPartner.setUseYn("Y");
        businessPartner.setCreatedBy(appUserId);
        businessPartner.setUpdatedBy(appUserId);

        applyRequest(
                businessPartner,
                request,
                businessNumber
        );

        businessPartnerMapper.insert(
                businessPartner
        );

        return getPartner(
                companyId,
                businessPartner.getPartnerId()
        );
    }

    @Transactional
    public BusinessPartnerResponse updatePartner(
            Long companyId,
            Long appUserId,
            Long partnerId,
            BusinessPartnerSaveRequest request
    ) {
        findPartner(companyId, partnerId);
        validatePartnerRole(request);

        String partnerCode =
                normalizePartnerCode(
                        request.getPartnerCode()
                );

        String businessNumber =
                normalizeBusinessNumber(
                        request.getBusinessNumber()
                );

        validateDuplicate(
                companyId,
                partnerCode,
                businessNumber,
                partnerId
        );

        BusinessPartner businessPartner =
                new BusinessPartner();

        businessPartner.setPartnerId(partnerId);
        businessPartner.setCompanyId(companyId);
        businessPartner.setPartnerCode(partnerCode);
        businessPartner.setUpdatedBy(appUserId);

        applyRequest(
                businessPartner,
                request,
                businessNumber
        );

        int updatedCount =
                businessPartnerMapper.update(
                        businessPartner
                );

        if (updatedCount == 0) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }

        return getPartner(companyId, partnerId);
    }

    @Transactional
    public void deactivatePartner(
            Long companyId,
            Long appUserId,
            Long partnerId
    ) {
        findPartner(companyId, partnerId);

        int updatedCount =
                businessPartnerMapper.updateUseYn(
                        companyId,
                        partnerId,
                        "N",
                        appUserId
                );

        if (updatedCount == 0) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }
    }

    private BusinessPartner findPartner(
            Long companyId,
            Long partnerId
    ) {
        BusinessPartner businessPartner =
                businessPartnerMapper.findById(
                        companyId,
                        partnerId
                );

        if (businessPartner == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND
            );
        }

        return businessPartner;
    }

    private void validateDuplicate(
            Long companyId,
            String partnerCode,
            String businessNumber,
            Long excludePartnerId
    ) {
        if (businessPartnerMapper
                .countByPartnerCode(
                        companyId,
                        partnerCode,
                        excludePartnerId
                ) > 0) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_PARTNER_CODE
            );
        }

        if (businessNumber != null
                && businessPartnerMapper
                .countByBusinessNumber(
                        companyId,
                        businessNumber,
                        excludePartnerId
                ) > 0) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_BUSINESS_NUMBER
            );
        }
    }

    private void validatePartnerRole(
            BusinessPartnerSaveRequest request
    ) {
        boolean supplier =
                "Y".equals(request.getSupplierYn());

        boolean customer =
                "Y".equals(request.getCustomerYn());

        if (!supplier && !customer) {
            throw new BusinessException(
                    ErrorCode.PARTNER_ROLE_REQUIRED
            );
        }
    }

    private void applyRequest(
            BusinessPartner businessPartner,
            BusinessPartnerSaveRequest request,
            String businessNumber
    ) {
        businessPartner.setPartnerName(
                request.getPartnerName().trim()
        );

        businessPartner.setBusinessNumber(
                businessNumber
        );

        businessPartner.setRepresentativeName(
                trimToNull(
                        request.getRepresentativeName()
                )
        );

        businessPartner.setContactName(
                trimToNull(request.getContactName())
        );

        businessPartner.setPhone(
                trimToNull(request.getPhone())
        );

        businessPartner.setEmail(
                trimToNull(request.getEmail())
        );

        businessPartner.setPostalCode(
                trimToNull(request.getPostalCode())
        );

        businessPartner.setAddress1(
                trimToNull(request.getAddress1())
        );

        businessPartner.setAddress2(
                trimToNull(request.getAddress2())
        );

        businessPartner.setSupplierYn(
                request.getSupplierYn()
        );

        businessPartner.setCustomerYn(
                request.getCustomerYn()
        );
    }

    private String normalizePartnerCode(
            String partnerCode
    ) {
        return partnerCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeBusinessNumber(
            String businessNumber
    ) {
        String normalized =
                trimToNull(businessNumber);

        if (normalized == null) {
            return null;
        }

        return normalized.replace("-", "");
    }

    private String normalizePartnerType(
            String partnerType
    ) {
        String normalized =
                normalizeUppercase(partnerType);

        if (normalized == null
                || "ALL".equals(normalized)) {
            return null;
        }

        if (!"SUPPLIER".equals(normalized)
                && !"CUSTOMER".equals(normalized)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return normalized;
    }

    private String normalizeUseYn(String useYn) {
        String normalized =
                normalizeUppercase(useYn);

        if (normalized == null
                || "ALL".equals(normalized)) {
            return null;
        }

        if (!"Y".equals(normalized)
                && !"N".equals(normalized)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return normalized;
    }

    private String normalizeUppercase(String value) {
        String normalized = trimToNull(value);

        if (normalized == null) {
            return null;
        }

        return normalized.toUpperCase(
                Locale.ROOT
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private BusinessPartnerResponse toResponse(
            BusinessPartner businessPartner
    ) {
        return new BusinessPartnerResponse(
                businessPartner.getPartnerId(),
                businessPartner.getCompanyId(),
                businessPartner.getPartnerCode(),
                businessPartner.getPartnerName(),
                businessPartner.getBusinessNumber(),
                businessPartner.getRepresentativeName(),
                businessPartner.getContactName(),
                businessPartner.getPhone(),
                businessPartner.getEmail(),
                businessPartner.getPostalCode(),
                businessPartner.getAddress1(),
                businessPartner.getAddress2(),
                businessPartner.getSupplierYn(),
                businessPartner.getCustomerYn(),
                businessPartner.getUseYn(),
                businessPartner.getCreatedAt(),
                businessPartner.getCreatedBy(),
                businessPartner.getUpdatedAt(),
                businessPartner.getUpdatedBy()
        );
    }
}