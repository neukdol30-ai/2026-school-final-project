package com.foodlogistics.erp.businesspartner;

import com.foodlogistics.erp.businesspartner.dto.BusinessPartnerResponse;
import com.foodlogistics.erp.businesspartner.dto.BusinessPartnerSaveRequest;
import com.foodlogistics.erp.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/business-partners")
@RequiredArgsConstructor
public class BusinessPartnerController {

    private final BusinessPartnerService businessPartnerService;

    @GetMapping
    @PreAuthorize("hasAuthority('BUSINESS_PARTNER_READ')")
    public ResponseEntity<
            ApiResponse<List<BusinessPartnerResponse>>
            > getPartners(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            String keyword,
            @RequestParam(required = false)
            String partnerType,
            @RequestParam(required = false)
            String useYn
    ) {
        List<BusinessPartnerResponse> response =
                businessPartnerService.getPartners(
                        getCompanyId(jwt),
                        keyword,
                        partnerType,
                        useYn
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @GetMapping("/{partnerId}")
    @PreAuthorize("hasAuthority('BUSINESS_PARTNER_READ')")
    public ResponseEntity<
            ApiResponse<BusinessPartnerResponse>
            > getPartner(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long partnerId
    ) {
        BusinessPartnerResponse response =
                businessPartnerService.getPartner(
                        getCompanyId(jwt),
                        partnerId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BUSINESS_PARTNER_CREATE')")
    public ResponseEntity<
            ApiResponse<BusinessPartnerResponse>
            > createPartner(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            BusinessPartnerSaveRequest request
    ) {
        BusinessPartnerResponse response =
                businessPartnerService.createPartner(
                        getCompanyId(jwt),
                        getAppUserId(jwt),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @PutMapping("/{partnerId}")
    @PreAuthorize("hasAuthority('BUSINESS_PARTNER_UPDATE')")
    public ResponseEntity<
            ApiResponse<BusinessPartnerResponse>
            > updatePartner(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long partnerId,
            @Valid @RequestBody
            BusinessPartnerSaveRequest request
    ) {
        BusinessPartnerResponse response =
                businessPartnerService.updatePartner(
                        getCompanyId(jwt),
                        getAppUserId(jwt),
                        partnerId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PatchMapping("/{partnerId}/deactivate")
    @PreAuthorize(
            "hasAuthority('BUSINESS_PARTNER_DEACTIVATE')"
    )
    public ResponseEntity<ApiResponse<Void>>
    deactivatePartner(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long partnerId
    ) {
        businessPartnerService.deactivatePartner(
                getCompanyId(jwt),
                getAppUserId(jwt),
                partnerId
        );

        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }

    private Long getCompanyId(Jwt jwt) {
        Number companyId = jwt.getClaim(
                "companyId"
        );

        return companyId.longValue();
    }

    private Long getAppUserId(Jwt jwt) {
        Number appUserId = jwt.getClaim(
                "appUserId"
        );

        return appUserId.longValue();
    }
}