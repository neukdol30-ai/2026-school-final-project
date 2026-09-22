package com.foodlogistics.erp.businesspartner;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BusinessPartnerMapper {

    List<BusinessPartner> findAll(
            @Param("companyId") Long companyId,
            @Param("keyword") String keyword,
            @Param("partnerType") String partnerType,
            @Param("useYn") String useYn
    );

    BusinessPartner findById(
            @Param("companyId") Long companyId,
            @Param("partnerId") Long partnerId
    );

    int countByPartnerCode(
            @Param("companyId") Long companyId,
            @Param("partnerCode") String partnerCode,
            @Param("excludePartnerId") Long excludePartnerId
    );

    int countByBusinessNumber(
            @Param("companyId") Long companyId,
            @Param("businessNumber") String businessNumber,
            @Param("excludePartnerId") Long excludePartnerId
    );

    int insert(BusinessPartner businessPartner);

    int update(BusinessPartner businessPartner);

    int updateUseYn(
            @Param("companyId") Long companyId,
            @Param("partnerId") Long partnerId,
            @Param("useYn") String useYn,
            @Param("updatedBy") Long updatedBy
    );
}
