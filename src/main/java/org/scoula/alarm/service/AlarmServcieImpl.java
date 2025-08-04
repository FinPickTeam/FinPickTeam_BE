package org.scoula.alarm.service;

import lombok.RequiredArgsConstructor;
import org.scoula.alarm.domain.AlarmVO;
import org.scoula.alarm.dto.AlarmDTO;
import org.scoula.alarm.mapper.AlarmMapper;
import org.scoula.user.util.NicknameGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmServcieImpl implements AlarmService {
    private final AlarmMapper alarmMapper;

    //알람DB에 저장하는 메소드
    @Override
    public void addAlarm(Long userId, String message) {
        alarmMapper.insert(userId, message);
    }

    @Override
    public void addAlarmAll(String message){
        alarmMapper.insertAll(message);
    }

    @Override
    public List<AlarmDTO> getAlarms(Long userId) {

        List<AlarmVO> alarmsVOS=alarmMapper.getAlarms(userId);
        List<AlarmDTO> alarmDTOS=new ArrayList<>();
        for (AlarmVO alarmVO : alarmsVOS) {
            alarmDTOS.add(AlarmDTO.of(alarmVO));
        }
        return alarmDTOS;
    }

    @Override
    public void updateAlarm(Long id) {
        alarmMapper.update(id);
    }

    @Override
    public void updateAll(Long userId) {
        alarmMapper.updateAll(userId);
    }
}
