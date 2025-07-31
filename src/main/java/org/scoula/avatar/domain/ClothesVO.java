package org.scoula.avatar.domain;

import lombok.Data;

@Data
public class ClothesVO {
    private int id;
    private int item_id;
    private int user_id;
    private int isWearing;
}
