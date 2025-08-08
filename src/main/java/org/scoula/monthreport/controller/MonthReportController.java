package org.scoula.monthreport.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.common.exception.BaseException;
import org.scoula.monthreport.dto.MonthReportDetailDto;
import org.scoula.monthreport.service.MonthReportGenerator;
import org.scoula.monthreport.service.MonthReportInitService;
import org.scoula.monthreport.service.MonthReportReadService;
import org.scoula.monthreport.util.MonthReportPdfGenerator;
import org.scoula.security.account.domain.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Api(tags = "monthreport-controller")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/monthreport")
public class MonthReportController {

    private final MonthReportInitService monthReportInitService;
    private final MonthReportReadService monthReportReadService;
    private final MonthReportGenerator monthReportGenerator;
    private final MonthReportPdfGenerator pdfGenerator;

    @ApiOperation(value = "월간 리포트 상세 조회", notes = "해당 월의 전체 리포트 데이터를 조회합니다.")
    @GetMapping("/{month}")
    public CommonResponseDTO<MonthReportDetailDto> getReport(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String month // ex: "2025-07"
    ) {
        Long userId = user.getUserId();
        MonthReportDetailDto dto = monthReportReadService.getReport(userId, month);
        return CommonResponseDTO.success("월간 리포트 조회 성공", dto);
    }

    @ApiOperation(value = "월간 리포트 초기 생성", notes = "ledger 기반으로 이번 달 이전의 리포트를 생성합니다.")
    @PostMapping("/init")
    public CommonResponseDTO<List<String>> initReports(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();
        List<String> result = monthReportInitService.generateAllMissingReports(userId);
        return CommonResponseDTO.success("총 " + result.size() + "개의 리포트가 생성되었습니다.", result);
    }

    @ApiOperation(value = "월간 리포트 수동 생성", notes = "테스트 용도. month를 받아 해당 월 리포트를 수동 생성합니다.")
    @PostMapping
    public CommonResponseDTO<String> generateManual(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam String month
    ) {
        Long userId = user.getUserId();
        monthReportGenerator.generate(userId, month);
        return CommonResponseDTO.success("리포트 수동 생성 완료");
    }

    @ApiOperation(value = "월간 리포트 PDF 다운로드", notes = "선택한 월의 리포트를 PDF로 저장합니다.")
    @GetMapping("/export")
    public void exportReport(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam String month,
            @RequestParam String format,
            HttpServletResponse response
    ) throws IOException {

        if (!"pdf".equalsIgnoreCase(format)) {
            throw new BaseException("지원하지 않는 포맷입니다", 400);
        }
        Long userId = user.getUserId();
        MonthReportDetailDto dto = monthReportReadService.getReport(userId, month);
        String html = pdfGenerator.buildHtmlFromDto(dto);
        byte[] pdfBytes = pdfGenerator.generateHtmlPdf(html);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=monthreport_" + month + ".pdf");
        response.getOutputStream().write(pdfBytes);
    }
}
