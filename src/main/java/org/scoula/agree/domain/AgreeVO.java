package org.scoula.agree.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgreeVO {
    private Long id; // user_id
    private boolean openBankingAgreed;
    private boolean personalInfoAgreed;
    private boolean arsAgreed;
    private LocalDateTime openBankingAgreedAt;
    private LocalDateTime personalInfoAgreedAt;
    private LocalDateTime arsAgreedAt;
}
