package org.scoula.news.dto;


import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;
import org.scoula.news.domain.NewsVO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Data
public class NaverNewsResponseDTO {
    private String lastBuildDate; // 검색 결과를 생성한 시간
    private int total;            // 총 검색 결과 개수
    private int start;            // 검색 시작 위치
    private int display;          // 한 번에 표시할 검색 결과 개수
    private List<Item> items;     // 검색 결과 뉴스 아이템 목록

    public List<NewsVO> toVO() {
        return this.items.stream()
                .map(Item::toNewsVO) // 각 Item을 NewsVO로 변환
                .collect(Collectors.toList()); // 리스트로 수집
    }

    @Data
    public static class Item {
        private String title;        // 뉴스 제목 (HTML 태그 포함 가능)
        private String originallink; // 뉴스 원본 링크
        private String link;         // 네이버 뉴스 링크
        private String description;  // 뉴스 요약 (HTML 태그 포함 가능)
        private String pubDate;      // 뉴스 발행일 (RFC 2822 형식)

        public NewsVO toNewsVO() {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            LocalDateTime parsePubDate=LocalDateTime.parse(this.pubDate,formatter);

            String unescapedTitle = StringEscapeUtils.unescapeHtml4(this.title);
            String unescapedDescription = StringEscapeUtils.unescapeHtml4(this.description);

            String cleanTitle = unescapedTitle.replaceAll("<[^>]*>", "");
            String cleanDescription = unescapedDescription.replaceAll("<[^>]*>", "");

            NewsVO newsVO = new NewsVO();
            newsVO.setTitle(cleanTitle);
            newsVO.setLink(this.originallink);
            newsVO.setSummary(cleanDescription);
            newsVO.setPublishedAt(parsePubDate);
            return newsVO;
        }
    }
}