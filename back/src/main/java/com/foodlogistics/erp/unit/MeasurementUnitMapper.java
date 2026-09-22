package com.foodlogistics.erp.unit;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MeasurementUnitMapper {

    List<MeasurementUnit> findAll(
            @Param("keyword") String keyword,
            @Param("useYn") String useYn
    );

    MeasurementUnit findById(
            @Param("unitId") Long unitId
    );

    int countByUnitCode(
            @Param("unitCode") String unitCode,
            @Param("excludeUnitId") Long excludeUnitId
    );

    int insert(MeasurementUnit measurementUnit);

    int update(MeasurementUnit measurementUnit);

    int updateUseYn(
            @Param("unitId") Long unitId,
            @Param("useYn") String useYn
    );
}