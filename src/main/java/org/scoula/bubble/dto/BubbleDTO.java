package org.scoula.bubble.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.bubble.domain.BubbleVO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BubbleDTO {
    private String message;

    public static BubbleDTO of(BubbleVO vo) {
        return new BubbleDTO(vo.getMessage());
    }
}
