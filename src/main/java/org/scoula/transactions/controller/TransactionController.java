package org.scoula.transactions.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.transactions.dto.TransactionDTO;
import org.scoula.transactions.dto.TransactionDetailDTO;
import org.scoula.transactions.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "transaction-controller")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @ApiOperation(value = "유저 기준 거래내역 조회", notes = "특정 유저 ID의 거래내역을 조회합니다. 거래일 기준 내림차순 정렬됩니다.")
    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<CommonResponseDTO<List<TransactionDTO>>> getTransactionsByUser(
            @ApiParam(value = "유저 ID") @PathVariable Long userId) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("거래내역 조회 성공", transactions));
    }

    @ApiOperation(value = "계좌 기준 거래내역 조회", notes = "특정 계좌 ID의 거래내역을 조회합니다. 거래일 기준 내림차순 정렬됩니다.")
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<CommonResponseDTO<List<TransactionDTO>>> getTransactionsByAccount(
            @ApiParam(value = "계좌 ID") @PathVariable Long accountId) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByAccountId(accountId);
        return ResponseEntity.ok(CommonResponseDTO.success("거래내역 조회 성공", transactions));
    }

    @ApiOperation(value = "거래 상세 조회", notes = "거래 ID로 상세 정보를 조회합니다. 소비 분석이 없는 경우 rule-based 분석 후 DB에 저장됩니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "성공"),
            @ApiResponse(code = 404, message = "거래 없음")
    })
    @GetMapping("/transactions/{id}")
    public ResponseEntity<CommonResponseDTO<TransactionDetailDTO>> getTransactionDetail(
            @ApiParam(value = "거래 ID") @PathVariable Long id) {
        TransactionDetailDTO detail = transactionService.getTransactionDetail(id);
        return ResponseEntity.ok(CommonResponseDTO.success("거래 상세 조회 성공", detail));
    }
}
