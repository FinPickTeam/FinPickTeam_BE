package org.scoula.challenge.service;

import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommonChallengeQueryServiceImpl implements CommonChallengeQueryService {

    private final SqlSessionTemplate sql;

    @Override
    public Map<String, Object> getCurrentCommonChallengeWithParticipation(Long userId) {
        Map<String, Object> res = new HashMap<>();

        // 1) 진행중 COMMON 1건
        Map<String, Object> challenge =
                sql.selectOne("org.scoula.challenge.rank.mapper.CommonQueryMapper.getInProgressCommonOne");

        if (challenge == null) {
            // 2) 폴백: 모집중 COMMON 1건
            challenge = sql.selectOne("org.scoula.challenge.rank.mapper.CommonQueryMapper.getRecruitingCommonOne");
        }

        boolean participating = false;
        if (challenge != null) {
            Long challengeId = ((Number) challenge.get("id")).longValue();
            Integer cnt = sql.selectOne("org.scoula.challenge.rank.mapper.CommonQueryMapper.isParticipating",
                    Map.of("userId", userId, "challengeId", challengeId));
            participating = (cnt != null && cnt > 0);
        }

        res.put("challenge", challenge);     // { id, title, goalValue, ... } 혹은 null
        res.put("participating", participating);
        return res;
    }
}
