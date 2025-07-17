package org.scoula.member.exception;

import org.scoula.member.dto.CommonResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Order(1)
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BaseException.class)
    @ResponseBody
    public ResponseEntity<CommonResponseDTO<Void>> handleBaseException(BaseException e) {
        log.error("🔥 BaseException 발생: {}", e.getMessage());
        return ResponseEntity
                .status(e.getStatus())
                .body(CommonResponseDTO.error(e.getMessage(), e.getStatus()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<CommonResponseDTO<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity
                .status(400)
                .body(CommonResponseDTO.error("잘못된 요청입니다: " + e.getMessage(), 400));
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<CommonResponseDTO<Void>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(500)
                .body(CommonResponseDTO.error("서버에서 오류가 발생했습니다: " + e.getMessage(), 500));
    }
}
