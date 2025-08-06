package org.scoula.agree.service;

import lombok.RequiredArgsConstructor;
import org.scoula.agree.dto.AgreeDTO;
import org.scoula.agree.mapper.AgreeMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgreeServiceImpl implements AgreeService {

    final private AgreeMapper agreeMapper;

    @Override
    public void createAgree(Long id) {
        agreeMapper.insert(id);
    }

    @Override
    public void updateOpenBankingAgree(Long userId) {
        AgreeDTO agreeDTO=AgreeDTO.builder()
                .id(userId)
                .openBankingAgreed(true)
                .build();

        agreeMapper.updateOpenBanking(agreeDTO.toVO());
    }

    @Override
    public void updatePersonalInfoAgree(Long userId) {
        AgreeDTO agreeDTO=AgreeDTO.builder()
                .id(userId)
                .personalInfoAgreed(true)
                .build();

        agreeMapper.updatePersonalInfo(agreeDTO.toVO());
    }

    @Override
    public void updateArsAgree(Long userId) {
        AgreeDTO agreeDTO=AgreeDTO.builder()
                .id(userId)
                .arsAgreed(true)
                .build();

        agreeMapper.updateArs(agreeDTO.toVO());
    }
}
