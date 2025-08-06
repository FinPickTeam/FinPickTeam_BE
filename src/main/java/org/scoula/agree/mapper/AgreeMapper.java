package org.scoula.agree.mapper;

import org.scoula.agree.domain.AgreeVO;

public interface AgreeMapper {
    void insert(Long userId);
    //오픈뱅킹동의정보 true로
    void updateOpenBanking(AgreeVO vo);
    //개인정보동의정보 true로
    void updatePersonalInfo(AgreeVO vo);
    //ars동의정보 true로
    void updateArs(AgreeVO vo);

}
