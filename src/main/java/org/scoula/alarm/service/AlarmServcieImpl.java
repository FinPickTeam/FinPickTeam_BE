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


    //특정 유저에게만 알람전송
    @Override
    public void addAlarm(Long userId, String message) {
        alarmMapper.insert(userId, message);
    }

    //모든 유저에게 알람전송
    @Override
    public void addAlarmAll(String message){
        alarmMapper.insertAll(message);
    }

    //알람 조회
    @Override
    public List<AlarmDTO> getAlarms(Long userId) {

        List<AlarmVO> alarmsVOS=alarmMapper.getAlarms(userId);
        List<AlarmDTO> alarmDTOS=new ArrayList<>();
        for (AlarmVO alarmVO : alarmsVOS) {
            alarmDTOS.add(AlarmDTO.of(alarmVO));
        }
        return alarmDTOS;
    }

    //특정 알람만 읽음처리
    @Override
    public void updateAlarm(Long id) {
        alarmMapper.update(id);
    }

    //모든 알람 읽음처리
    @Override
    public void updateAll(Long userId) {
        alarmMapper.updateAll(userId);
    }
}
