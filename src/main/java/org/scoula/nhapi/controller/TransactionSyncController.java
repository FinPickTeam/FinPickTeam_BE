package org.scoula.nhapi.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.nhapi.service.TransactionSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nh")
@Api(tags = "nhapi-controller")
public class TransactionSyncController {

    private final TransactionSyncService transactionSyncService;

    @PostMapping("/sync")
    public ResponseEntity<CommonResponseDTO<Map<String, Object>>> syncTransactions(
            @RequestParam String finAccount,
            @RequestParam String fromDate,
            @RequestParam String toDate) {

        int saved = transactionSyncService.syncTransaction(finAccount, fromDate, toDate);
        return ResponseEntity.ok(CommonResponseDTO.success(saved + "건의 거래내역이 동기화되었습니다."));
    }

}
