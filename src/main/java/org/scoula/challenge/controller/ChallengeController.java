package org.scoula.challenge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.challenge.dto.*;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.enums.ChallengeType;
import org.scoula.challenge.service.ChallengeService;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.security.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Component
@RestController
@RequestMapping("/api/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public CommonResponseDTO<ChallengeCreateResponseDTO> createChallenge(
            @RequestBody ChallengeCreateRequestDTO req,
            HttpServletRequest request) {

        String bearer = request.getHeader("Authorization");
        Long userId = jwtUtil.getIdFromToken(bearer.replace("Bearer ", ""));

        ChallengeCreateResponseDTO result = challengeService.createChallenge(userId, req);
        return CommonResponseDTO.success("챌린지가 생성되었습니다.", result);
    }

    @GetMapping("/list")
    public CommonResponseDTO<List<ChallengeListResponseDTO>> getChallenges(
            @RequestParam(value = "type", required = false) ChallengeType type,
            @RequestParam(value = "status", required = false) ChallengeStatus status,
            HttpServletRequest request) {

        String bearer = request.getHeader("Authorization");
        Long userId = jwtUtil.getIdFromToken(bearer.replace("Bearer ", ""));

        List<ChallengeListResponseDTO> challenges = challengeService.getChallenges(userId, type, status);
        return CommonResponseDTO.success("챌린지 리스트 조회 성공", challenges);
    }

    @GetMapping("/{id}")
    public CommonResponseDTO<ChallengeDetailResponseDTO> getChallengeDetail(@PathVariable("id") Long id,
                                                                            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        Long userId = jwtUtil.getIdFromToken(bearer.replace("Bearer ", ""));
        ChallengeDetailResponseDTO detail = challengeService.getChallengeDetail(userId, id);
        return CommonResponseDTO.success("챌린지 상세 조회 성공", detail);
    }

    @PostMapping("/{id}/join")
    public CommonResponseDTO<?> joinChallenge(@PathVariable("id") Long challengeId,
                                              @RequestBody(required = false) ChallengeJoinRequestDTO joinRequest,
                                              HttpServletRequest request) {

        String bearer = request.getHeader("Authorization");
        Long userId = jwtUtil.getIdFromToken(bearer.replace("Bearer ", ""));

        Integer password = (joinRequest != null) ? joinRequest.getPassword() : null;

        challengeService.joinChallenge(userId, challengeId, password);
        return CommonResponseDTO.success("챌린지 참여 신청이 완료되었습니다.");
    }

    @GetMapping("/summary")
    public CommonResponseDTO<ChallengeSummaryResponseDTO> getChallengeSummary(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        Long userId = jwtUtil.getIdFromToken(bearer.replace("Bearer ", ""));

        ChallengeSummaryResponseDTO summary = challengeService.getChallengeSummary(userId);
        return CommonResponseDTO.success("챌린지 요약 정보 조회 성공", summary);
    }

    @GetMapping("/{id}/result")
    public CommonResponseDTO<ChallengeResultResponseDTO> getChallengeResult(@PathVariable("id") Long challengeId,
                                                                            HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        Long userId = jwtUtil.getIdFromToken(bearer.replace("Bearer ", ""));
        ChallengeResultResponseDTO result = challengeService.getChallengeResult(userId, challengeId);
        return CommonResponseDTO.success("챌린지 결과 조회 성공", result);
    }

    @PatchMapping("/{id}/result/confirm")
    public CommonResponseDTO<?> confirmChallengeResult(@PathVariable("id") Long challengeId,
                                                       HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        Long userId = jwtUtil.getIdFromToken(bearer.replace("Bearer ", ""));
        challengeService.confirmChallengeResult(userId, challengeId);
        return CommonResponseDTO.success("챌린지 결과 확인 처리 완료");
    }

    @GetMapping("/has-unconfirmed")
    public CommonResponseDTO<Boolean> hasUnconfirmedChallengeResult(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        Long userId = jwtUtil.getIdFromToken(bearer.replace("Bearer ", ""));
        boolean result = challengeService.hasUnconfirmedResult(userId);
        return CommonResponseDTO.success("미확인 결과 존재 여부 확인", result);
    }


}
