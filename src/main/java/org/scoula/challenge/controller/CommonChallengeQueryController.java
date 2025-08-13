package org.scoula.challenge.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.challenge.rank.mapper.CommonQueryMapper;
import org.scoula.challenge.rank.dto.CommonChallengeSimpleDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/challenge/common")
@RequiredArgsConstructor
public class CommonChallengeQueryController {

    private final CommonQueryMapper mapper;

    @GetMapping("/current")
    public CommonResponseDTO<?> currentCommon(
            // ✅ SpEL: CustomUserDetails의 id 필드를 바로 Long으로 주입 (필드명이 다르면 "userId"/"memberId"로 변경)
            @AuthenticationPrincipal(expression = "id") Long meId
    ) {
        CommonChallengeSimpleDTO challenge = mapper.getInProgressCommonOne();
        if (challenge == null) {
            challenge = mapper.getRecruitingCommonOne();
        }

        boolean participating = false;
        if (challenge != null && meId != null) {
            Integer cnt = mapper.isParticipating(meId, challenge.getId());
            participating = (cnt != null && cnt > 0);
        }

        return CommonResponseDTO.ok(Map.of(
                "challenge", challenge,
                "participating", participating
        ));
    }
}
