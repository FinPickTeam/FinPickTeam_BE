package org.scoula.monthreport.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.monthreport.service.MonthReportInitService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "monthreport-controller")
@RestController
@RequestMapping("/api/internal/monthreport")
@RequiredArgsConstructor
public class MonthReportInitController {

    private final MonthReportInitService monthReportInitService;

    @ApiOperation(value = "월간 리포트 초기 일괄 생성", notes = "card_transaction 기반으로 거래 발생 월 전체에 대해 리포트를 생성합니다.")
    @PostMapping("/init")
    public CommonResponseDTO<List<String>> initMonthReports(@RequestBody InitRequest request) {
        List<String> result = monthReportInitService.generateAllMissingReports(request.getUserId());
        return CommonResponseDTO.success("총 " + result.size() + "개의 리포트가 생성되었습니다.", result);
    }

    @Getter
    @Setter
    public static class InitRequest {
        private Long userId;
    }
}
