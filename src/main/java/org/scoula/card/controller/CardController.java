package org.scoula.card.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.card.dto.CardDto;
import org.scoula.card.dto.CardRegisterResponseDto;
import org.scoula.card.service.CardService;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.nhapi.dto.FinCardRequestDto;
import org.scoula.security.account.domain.CustomUserDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/register")
    public ResponseEntity<CommonResponseDTO<CardRegisterResponseDto>> registerCard(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody(required = false) FinCardRequestDto dto // ★ 바디 옵셔널
    ) {
        Long userId = user.getUserId();
        CardRegisterResponseDto response = cardService.registerCard(userId, dto);
        return ResponseEntity.ok(CommonResponseDTO.success("카드 등록 및 승인내역 저장 완료", response));
    }

    @PostMapping("/{cardId}/sync")
    public ResponseEntity<CommonResponseDTO<Void>> syncCardData(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long cardId) {
        Long userId = user.getUserId();
        cardService.syncCardById(userId, cardId);
        return ResponseEntity.ok(CommonResponseDTO.success("카드 승인내역 동기화 완료"));
    }

    @PostMapping("/sync-all")
    public ResponseEntity<CommonResponseDTO<Void>> syncAllCards(
            @AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getUserId();
        cardService.syncAllCardsByUserId(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("사용자의 모든 카드 승인내역 동기화 완료"));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<CommonResponseDTO<Void>> deleteCard(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long cardId) {
        Long userId = user.getUserId();
        cardService.deactivateCard(cardId, userId);
        return ResponseEntity.ok(CommonResponseDTO.success("카드 비활성화 완료"));
    }

    @GetMapping("/list")
    public ResponseEntity<CommonResponseDTO<List<CardDto>>> getCardsWithTotal(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        Long userId = user.getUserId();
        YearMonth targetMonth = (month != null) ? month : YearMonth.now();
        List<CardDto> result = cardService.getCardsWithMonth(userId, targetMonth);
        return ResponseEntity.ok(CommonResponseDTO.success("카드 목록 조회 성공", result));
    }
}
