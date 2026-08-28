package com.foodlogistics.erp.security.handler;

import com.foodlogistics.erp.common.exception.ErrorCode;
import com.foodlogistics.erp.common.response.ApiError;
import com.foodlogistics.erp.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;

        ApiError apiError = ApiError.of(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        ApiResponse<Void> body = ApiResponse.fail(apiError);
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        jsonMapper.writeValue(response.getWriter(), body);
    }
}
