package com.foodlogistics.erp.masterdata.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SupplierOptionResponse {

    private final Long supplierId;
    private final String supplierCode;
    private final String supplierName;
}