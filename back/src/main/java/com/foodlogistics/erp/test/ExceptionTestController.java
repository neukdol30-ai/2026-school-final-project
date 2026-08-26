package com.foodlogistics.erp.test;

import com.foodlogistics.erp.common.exception.BusinessException;
import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/exceptions")
public class ExceptionTestController {

    @PostMapping("/validation")
    public ApiResponse<ExceptionTestRequest> validation(
            @Valid @RequestBody ExceptionTestRequest request
    ) {
        return ApiResponse.ok(request);
    }

    @GetMapping("/business")
    public ApiResponse<Void> business() {
        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @GetMapping("/unexpected")
    public ApiResponse<Void> unexpected() {
        throw new IllegalStateException("외부에 노출되면 안 되는 내부 오류");
    }
}