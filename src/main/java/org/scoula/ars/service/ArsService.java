package org.scoula.ars.service;

import org.scoula.ars.domain.ArsVO;
import org.scoula.ars.dto.ArsDTO;

public interface ArsService {
    void updateArs(Long userId , ArsDTO dto);
    ArsDTO getArs(Long userId);
}
