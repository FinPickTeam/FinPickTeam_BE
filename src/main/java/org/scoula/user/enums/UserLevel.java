package org.scoula.user.enums;

public enum UserLevel {
    SEEDLING("금융새싹",1L),
    TRAINEE("금융견습",2L),
    WIZARD("금융법사",3L),
    MASTER("금융도사",4L);

    private final String label;
    private final Long itemId;

    UserLevel(String label, Long itemId) {
        this.label = label;
        this.itemId = itemId;
    }

    public String getLabel() {
        return label;
    }

    public Long getItemId() {
        return itemId;
    }

    public static UserLevel getLevelForPoints(int points) {
        if (points >= 60000) return MASTER;
        if (points >= 40000) return WIZARD;
        if (points >= 20000) return TRAINEE;
        return SEEDLING;
    }
}
