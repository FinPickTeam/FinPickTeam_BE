package org.scoula.agree.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.agree.domain.AgreeVO;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgreeDTO {
    private Long id;
    private boolean openBankingAgreed;
    private boolean personalInfoAgreed;
    private boolean arsAgreed;
    private LocalDateTime openBankingAgreedAt;
    private LocalDateTime personalInfoAgreedAt;
    private LocalDateTime arsAgreedAt;


    public static AgreeDTO of(AgreeVO vo) {
        return AgreeDTO.builder()
                .id(vo.getId())
                .openBankingAgreed(vo.isOpenBankingAgreed())
                .personalInfoAgreed(vo.isPersonalInfoAgreed())
                .arsAgreed(vo.isArsAgreed())
                .openBankingAgreedAt(vo.getOpenBankingAgreedAt())
                .personalInfoAgreedAt(vo.getPersonalInfoAgreedAt())
                .arsAgreedAt(vo.getArsAgreedAt())
                .build();
    }

    public AgreeVO toVO() {
        AgreeVO vo = new AgreeVO();
        vo.setId(this.id);
        vo.setOpenBankingAgreed(this.openBankingAgreed);
        vo.setPersonalInfoAgreed(this.personalInfoAgreed);
        vo.setArsAgreed(this.arsAgreed);
        vo.setOpenBankingAgreedAt(this.openBankingAgreedAt);
        vo.setPersonalInfoAgreedAt(this.personalInfoAgreedAt);
        vo.setArsAgreedAt(this.arsAgreedAt);
        return vo;
    }
}