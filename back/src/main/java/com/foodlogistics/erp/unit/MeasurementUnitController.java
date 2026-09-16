package com.foodlogistics.erp.unit;

import com.foodlogistics.erp.common.response.ApiResponse;
import com.foodlogistics.erp.unit.dto.MeasurementUnitResponse;
import com.foodlogistics.erp.unit.dto.MeasurementUnitSaveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class MeasurementUnitController {

    private final MeasurementUnitService measurementUnitService;

    @GetMapping
    @PreAuthorize("hasAuthority('UNIT_READ')")
    public ResponseEntity<
            ApiResponse<List<MeasurementUnitResponse>>
            > getUnits(
            @RequestParam(required = false)
            String keyword,
            @RequestParam(required = false)
            String useYn
    ) {
        List<MeasurementUnitResponse> response =
                measurementUnitService.getUnits(
                        keyword,
                        useYn
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @GetMapping("/{unitId}")
    @PreAuthorize("hasAuthority('UNIT_READ')")
    public ResponseEntity<
            ApiResponse<MeasurementUnitResponse>
            > getUnit(
            @PathVariable Long unitId
    ) {
        MeasurementUnitResponse response =
                measurementUnitService.getUnit(unitId);

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('UNIT_CREATE')")
    public ResponseEntity<
            ApiResponse<MeasurementUnitResponse>
            > createUnit(
            @Valid @RequestBody
            MeasurementUnitSaveRequest request
    ) {
        MeasurementUnitResponse response =
                measurementUnitService.createUnit(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @PutMapping("/{unitId}")
    @PreAuthorize("hasAuthority('UNIT_UPDATE')")
    public ResponseEntity<
            ApiResponse<MeasurementUnitResponse>
            > updateUnit(
            @PathVariable Long unitId,
            @Valid @RequestBody
            MeasurementUnitSaveRequest request
    ) {
        MeasurementUnitResponse response =
                measurementUnitService.updateUnit(
                        unitId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PatchMapping("/{unitId}/deactivate")
    @PreAuthorize("hasAuthority('UNIT_DEACTIVATE')")
    public ResponseEntity<ApiResponse<Void>>
    deactivateUnit(
            @PathVariable Long unitId
    ) {
        measurementUnitService.deactivateUnit(unitId);

        return ResponseEntity.ok(
                ApiResponse.ok()
        );
    }
}