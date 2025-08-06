package org.scoula.user.enums;

public enum UserLevel {
    SEEDLING("금융새싹"),
    TRAINEE("금융견습"),
    WIZARD("금융법사"),
    MASTER("금융도사");

    private final String label;

    UserLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static UserLevel getLevelForPoints(int points) {
        if (points >= 60000) return MASTER;
        if (points >= 40000) return WIZARD;
        if (points >= 20000) return TRAINEE;
        return SEEDLING;
    }
}
