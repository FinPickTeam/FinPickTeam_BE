package org.scoula.alarm.mapper;

import org.scoula.alarm.domain.AlarmVO;

import java.util.List;

public interface AlarmMapper {

    void insert(Long userId, String message);

    void insertAll(String message);

    List<AlarmVO> getAlarms(Long userId);

    void update(Long id);

    void updateAll(Long userId);

}
