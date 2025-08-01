package org.scoula.avatar.domain;

import lombok.Data;

@Data
public class ItemsVO {
    private Long id;
    private String name;
    private String type;
    private int cost;
    private String imageUrl;
}
