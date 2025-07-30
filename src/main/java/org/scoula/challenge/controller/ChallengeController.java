package org.scoula.challenge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.challenge.dto.ChallengeCreateRequestDTO;
import org.scoula.challenge.dto.ChallengeCreateResponseDTO;
import org.scoula.challenge.service.ChallengeService;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.security.util.JwtUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
}
