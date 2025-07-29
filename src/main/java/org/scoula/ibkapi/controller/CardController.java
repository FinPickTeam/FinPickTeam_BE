package org.scoula.ibkapi.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.scoula.ibkapi.service.CardService;
import org.scoula.common.dto.CommonResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ibk")
@Api(tags = "ibkapi-controller")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/sync-cards")
    public ResponseEntity<CommonResponseDTO<Void>> syncCardList(@RequestParam Long userId) {
        cardService.syncCardList(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("카드 목록 동기화 완료"));
    }

    @PostMapping("/sync-transactions")
    public ResponseEntity<CommonResponseDTO<Void>> syncCardTransactions(@RequestParam Long userId) {
        cardService.syncCardTransactions(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("카드 승인내역 동기화 완료"));
    }
}
