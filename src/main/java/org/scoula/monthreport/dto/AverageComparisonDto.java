package org.scoula.monthreport.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;

@ApiModel("AverageComparison")
public class AverageComparisonDto {
    @ApiModelProperty("전체 소비 증감률(또래 평균 대비, %)")
    public Integer totalDiffPct;
    @ApiModelProperty("카테고리별 증감률(%)")
    public Map<String, Integer> byCategory;
    @ApiModelProperty("요약 코멘트")
    public String comment;

    public AverageComparisonDto(Integer totalDiffPct, Map<String, Integer> byCategory, String comment) {
        this.totalDiffPct = totalDiffPct;
        this.byCategory = byCategory;
        this.comment = comment;
    }
}
