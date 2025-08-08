package org.scoula.finance.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class WishListDto {
    private Long userId;
    private String productType;
    private String productId;
}
