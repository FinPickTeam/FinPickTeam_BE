
package org.scoula.summary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AssetTotalDto {
    private BigDecimal totalAsset;
}
