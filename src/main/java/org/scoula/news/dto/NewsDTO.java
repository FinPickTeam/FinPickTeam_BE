package org.scoula.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.news.domain.NewsVO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsDTO {
    private Long id;
    private String title;
    private String summary;
    private String link;
    private LocalDateTime publishedAt;
    private String keyword;


    public static NewsDTO of(NewsVO newsVO){

        return NewsDTO.builder()
                .id(newsVO.getId())
                .title(newsVO.getTitle())
                .summary(newsVO.getSummary())
                .link(newsVO.getLink())
                .publishedAt(newsVO.getPublishedAt())
                .keyword(newsVO.getKeyword())
                .build();
    }

    public NewsVO toVO(){
        NewsVO newsVO = new NewsVO();
        newsVO.setId(id);
        newsVO.setTitle(title);
        newsVO.setSummary(summary);
        newsVO.setLink(link);
        newsVO.setPublishedAt(publishedAt);
        newsVO.setKeyword(keyword);
        return newsVO;
    }
}
