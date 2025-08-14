package org.scoula.challenge.rank.model;

public class ParticipantInfo {
    private Long userChallengeId;
    private int actualValue;

    public ParticipantInfo() {} // MyBatis용 기본 생성자

    public ParticipantInfo(Long userChallengeId, int actualValue) {
        this.userChallengeId = userChallengeId;
        this.actualValue = actualValue;
    }

    public Long getUserChallengeId() { return userChallengeId; }
    public void setUserChallengeId(Long userChallengeId) { this.userChallengeId = userChallengeId; }

    public int getActualValue() { return actualValue; }
    public void setActualValue(int actualValue) { this.actualValue = actualValue; }
}
