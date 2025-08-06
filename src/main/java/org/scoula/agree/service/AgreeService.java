package org.scoula.agree.service;

import org.scoula.agree.domain.AgreeVO;

public interface AgreeService {
    //약관동의정보 만들기
    void createAgree(Long id);
    //오픈뱅킹동의정보 true로
    void updateOpenBankingAgree(Long userId);
    //개인정보동의정보 true로
    void updatePersonalInfoAgree(Long userId);
    //ars동의정보 true로
    void updateArsAgree(Long userId);
}
