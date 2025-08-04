package org.scoula.card.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.card.dto.CardRegisterResponseDto;
import org.scoula.card.service.CardService;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.nhapi.dto.FinCardRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/register")
    public ResponseEntity<CommonResponseDTO<CardRegisterResponseDto>> registerCard(@RequestBody FinCardRequestDto dto) {
        CardRegisterResponseDto response = cardService.registerCard(dto);
        return ResponseEntity.ok(CommonResponseDTO.success("카드 등록 및 승인내역 저장 완료", response));
    }

    @PostMapping("/{cardId}/sync")
    public ResponseEntity<CommonResponseDTO<Void>> syncCardData(@PathVariable Long cardId) {
        cardService.syncCardById(cardId);
        return ResponseEntity.ok(CommonResponseDTO.success("카드 승인내역 동기화 완료"));
    }

    @PostMapping("/sync-all/{userId}")
    public ResponseEntity<CommonResponseDTO<Void>> syncAllCards(@PathVariable Long userId) {
        cardService.syncAllCardsByUserId(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("사용자의 모든 카드 승인내역 동기화 완료"));
    }
}
