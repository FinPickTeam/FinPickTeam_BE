package org.scoula.challenge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.challenge.dto.ChallengeCreateRequestDTO;
import org.scoula.challenge.dto.ChallengeCreateResponseDTO;
import org.scoula.challenge.dto.ChallengeListResponseDTO;
import org.scoula.challenge.enums.ChallengeStatus;
import org.scoula.challenge.enums.ChallengeType;
import org.scoula.challenge.service.ChallengeService;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.security.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
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

}
