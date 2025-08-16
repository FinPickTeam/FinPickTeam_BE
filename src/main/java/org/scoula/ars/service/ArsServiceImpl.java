package org.scoula.ars.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.ars.domain.ArsVO;
import org.scoula.ars.dto.ArsDTO;
import org.scoula.ars.mapper.ArsMapper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Log4j2
public class ArsServiceImpl implements ArsService {
    final private ArsMapper arsMapper;

    @Override
    public void updateArs(Long userId, ArsDTO dto) {
        ArsVO vo=dto.toVO();
        arsMapper.update(userId, vo);
    }

    @Override
    public ArsDTO getArs(Long userId) {
        ArsVO vo = arsMapper.findById(userId);
        return ArsDTO.of(vo);
    }
}
