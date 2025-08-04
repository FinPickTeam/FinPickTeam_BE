package org.scoula.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.alarm.domain.AlarmVO;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmDTO {
    private Long id;
    private Long userId;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;


    public static AlarmDTO of(AlarmVO alarmVO) {
        return AlarmDTO.builder()
                .id(alarmVO.getId())
                .userId(alarmVO.getUserId())
                .message(alarmVO.getMessage())
                .isRead(alarmVO.isRead())
                .createdAt(alarmVO.getCreatedAt())
                .build();
    }

    public AlarmVO toVO() {
        AlarmVO alarmVO = new AlarmVO();
        alarmVO.setId(this.id);
        alarmVO.setUserId(this.userId);
        alarmVO.setMessage(this.message);
        alarmVO.setRead(this.isRead);
        alarmVO.setCreatedAt(this.createdAt);
        return alarmVO;
    }
}