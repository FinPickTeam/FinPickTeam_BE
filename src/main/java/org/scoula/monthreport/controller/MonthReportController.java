package org.scoula.monthreport.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.monthreport.dto.MonthReportDetailDto;
import org.scoula.monthreport.service.MonthReportGenerator;
import org.scoula.monthreport.service.MonthReportInitService;
import org.scoula.monthreport.service.MonthReportReadService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "monthreport-controller")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/monthreport")
public class MonthReportController {

    private final MonthReportInitService monthReportInitService;
    private final MonthReportReadService monthReportReadService;
    private final MonthReportGenerator monthReportGenerator;

    @ApiOperation(value = "월간 리포트 상세 조회", notes = "해당 월의 전체 리포트 데이터를 조회합니다.")
    @GetMapping("/{userId}/{month}")
    public CommonResponseDTO<MonthReportDetailDto> getReport(
            @PathVariable Long userId,
            @PathVariable String month // ex: "2025-07"
    ) {
        MonthReportDetailDto dto = monthReportReadService.getReport(userId, month);
        return CommonResponseDTO.success("월간 리포트 조회 성공", dto);
    }

    @ApiOperation(value = "월간 리포트 초기 생성", notes = "ledger 기반으로 이번 달 이전의 리포트를 생성합니다.")
    @PostMapping("/init")
    public CommonResponseDTO<List<String>> initReports(@RequestBody InitRequest request) {
        List<String> result = monthReportInitService.generateAllMissingReports(request.getUserId());
        return CommonResponseDTO.success("총 " + result.size() + "개의 리포트가 생성되었습니다.", result);
    }

    @ApiOperation(value = "월간 리포트 수동 생성", notes = "테스트 용도. userId, month를 받아 해당 월 리포트를 수동 생성합니다.")
    @PostMapping
    public CommonResponseDTO<String> generateManual(
            @RequestParam Long userId,
            @RequestParam String month
    ) {
        monthReportGenerator.generate(userId, month);
        return CommonResponseDTO.success("리포트 수동 생성 완료");
    }

    @Getter
    @Setter
    public static class InitRequest {
        private Long userId;
    }
}
