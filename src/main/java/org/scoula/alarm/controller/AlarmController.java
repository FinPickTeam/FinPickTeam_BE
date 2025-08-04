package org.scoula.alarm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.alarm.dto.AlarmDTO;
import org.scoula.alarm.service.AlarmService;
import org.scoula.common.dto.CommonResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Log4j2
@Api(tags = "알람api", description="alarm controller")
@RequestMapping("/api/alarm")
public class AlarmController {
    final private AlarmService alarmService;

    @ApiOperation(value="알람 조회", notes="현재 존재하는 알람들을 조회합니다.")
    @PostMapping("/userId={userId}")
    public ResponseEntity<CommonResponseDTO<List<AlarmDTO>>> getAlarm(@PathVariable Long userId) {
        List<AlarmDTO> alarmDTO=alarmService.getAlarms(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("알람 조회 성공", alarmDTO));
    }

    @ApiOperation(value="특정 알람 읽음여부 수정", notes = "특정 알람의 읽음여부를 true로 변경합니다.")
    @PutMapping("/update")
    public ResponseEntity<CommonResponseDTO<String>> updateAlarm(@RequestParam Long AlarmId) {
        alarmService.updateAlarm(AlarmId);
        return ResponseEntity.ok(CommonResponseDTO.success("알람상태 수정 성공"));
    }

    @ApiOperation(value="모든 알람 읽음여부 수정", notes = "유저에게 전달된 모든 알람의 읽음여부를 true로 변경합니다.")
    @PutMapping("/updateAll")
    public ResponseEntity<CommonResponseDTO<String>> updateAll(@RequestParam Long userId) {
        alarmService.updateAll(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("전체알람상태 수정 성공"));
    }
}
