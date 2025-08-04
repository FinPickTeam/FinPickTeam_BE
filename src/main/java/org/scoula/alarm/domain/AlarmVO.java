package org.scoula.alarm.domain;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class AlarmVO {
    private Long id;
    private Long userId;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;
}