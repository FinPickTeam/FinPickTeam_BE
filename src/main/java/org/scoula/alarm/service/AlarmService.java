package org.scoula.alarm.service;

import org.scoula.alarm.domain.AlarmVO;
import org.scoula.alarm.dto.AlarmDTO;

import java.util.List;

public interface AlarmService {

    void addAlarm(Long userId, String message);

    void addAlarmAll(String message);

    List<AlarmDTO> getAlarms(Long userId);

    void updateAlarm(Long id);

    void updateAll(Long userId);
}
