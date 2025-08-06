package org.scoula.challenge.rank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeCoinRankSnapshotResponseDTO {
    private Long id;             // snapshot row id
    private Long userId;         // 유저 ID
    private String nickname;     // 유저 닉네임
    private int rank;            // 랭킹
    private long totalCoin;      // 누적 포인트
    private String month;        // YYYY-MM
}
