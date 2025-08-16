package org.scoula.ars.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.ars.domain.ArsVO;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ArsDTO {

    private String userName;

    private String phoneNum;

    private LocalDate birthday;

    public ArsVO toVO(){
        ArsVO arsVO = new ArsVO();
        arsVO.setUserName(userName);
        arsVO.setPhoneNum(phoneNum);
        arsVO.setBirthday(birthday);
        return arsVO;
    }

    public static ArsDTO of(ArsVO vo) {
        ArsDTO dto = new ArsDTO();
        dto.setUserName(vo.getUserName());
        dto.setPhoneNum(vo.getPhoneNum());
        dto.setBirthday(vo.getBirthday());
        return dto;
    }
}