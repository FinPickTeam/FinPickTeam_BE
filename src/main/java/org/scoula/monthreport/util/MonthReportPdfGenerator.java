package org.scoula.monthreport.util;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.scoula.monthreport.dto.MonthReportDetailDto;
import org.scoula.monthreport.dto.SpendingPatternDto;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.stream.Collectors;

@Component
public class MonthReportPdfGenerator {

    public byte[] generateHtmlPdf(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null); // baseUri: null or public dir
            InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/NanumGothic.ttf");
            builder.useFont(() -> fontStream, "NanumGothic", 400, BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.defaultTextDirection(PdfRendererBuilder.TextDirection.LTR);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF 변환 실패", e);
        }
    }

    public String buildHtmlFromDto(MonthReportDetailDto dto) {
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>");
        sb.append("<html lang='ko'><head>");
        sb.append("<meta charset='UTF-8'/>");
        sb.append("<style>");
        sb.append("body { font-family: 'NanumGothic'; padding: 30px; }");
        sb.append("h1, h2, h3 { color: #333; }");
        sb.append("table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }");
        sb.append("th, td { border: 1px solid #999; padding: 8px; text-align: left; font-size: 12px; }");
        sb.append("th { background-color: #f2f2f2; }");
        sb.append("ul { padding-left: 16px; font-size: 13px; }");
        sb.append(".section { margin-bottom: 30px; }");
        sb.append(".label { font-weight: bold; }");
        sb.append(".highlight { color: #8a2be2; font-weight: bold; }");
        sb.append("</style>");
        sb.append("</head><body>");

        sb.append("<h1>").append(dto.getMonth()).append(" 월간 리포트</h1>");

        // 📊 소비 요약
        sb.append("<div class='section'>");
        sb.append("<h2>📊 소비 요약</h2>");
        sb.append("<p><span class='label'>총 지출:</span> ").append(dto.getTotalExpense()).append("원</p>");
        sb.append("<p><span class='label'>지난달 대비:</span> <span class='highlight'>")
                .append(dto.getCompareExpense()).append("원</span></p>");
        sb.append("</div>");

        // 📌 카테고리 비율
        sb.append("<div class='section'>");
        sb.append("<h3>📌 카테고리 비율</h3>");
        sb.append("<table><tr><th>카테고리</th><th>비율</th></tr>");
        dto.getCategoryChart().forEach(c -> {
            sb.append("<tr><td>").append(c.getCategory()).append("</td><td>").append(c.getRatio()).append("%</td></tr>");
        });
        sb.append("</table>");
        sb.append("</div>");

        // 🔥 Top 3 소비
        sb.append("<div class='section'>");
        sb.append("<h3>🔥 이번 달 지출 TOP 3</h3>");
        sb.append("<table><tr><th>카테고리</th><th>금액</th><th>비율</th></tr>");
        dto.getTop3Spending().forEach(t -> {
            sb.append("<tr><td>").append(t.getCategory()).append("</td><td>")
                    .append(t.getAmount()).append("원</td><td>").append(t.getRatio()).append("%</td></tr>");
        });
        sb.append("</table>");
        sb.append("</div>");

        // 🧠 소비 성향
        sb.append("<div class='section'>");
        sb.append("<h3>🧠 소비 성향 분석</h3>");
        sb.append("<p><span class='label'>소비 성향:</span> ");
        if (dto.getSpendingPatterns() != null && !dto.getSpendingPatterns().isEmpty()) {
            sb.append(
                    dto.getSpendingPatterns().stream()
                            .map(SpendingPatternDto::getLabel)
                            .collect(Collectors.joining(" / "))
            );
        } else {
            sb.append("없음");
        }
        sb.append("</p>");
// 상세 설명도 보여주고 싶으면 아래처럼
        sb.append("<ul>");
        dto.getSpendingPatterns().forEach(p ->
                sb.append("<li>").append(p.getDesc()).append("</li>")
        );
        sb.append("</ul>");
        sb.append("<p>").append(dto.getSpendingPatternFeedback()).append("</p>");
        sb.append("</div>");


        // 🎯 챌린지
        sb.append("<div class='section'>");
        sb.append("<h3>🎯 다음 달 추천 챌린지</h3>");
        sb.append("<ul>");
        dto.getRecommendedChallenges().forEach(c -> {
            sb.append("<li>").append(c.getTitle()).append(" – ").append(c.getDescription()).append("</li>");
        });
        sb.append("</ul>");
        sb.append("</div>");

        sb.append("</body></html>");
        return sb.toString();
    }



}
