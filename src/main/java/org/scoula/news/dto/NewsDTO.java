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




    public static NewsDTO of(NewsVO newsVO){

        return NewsDTO.builder()
                .id(newsVO.getId())
                .title(newsVO.getTitle())
                .summary(newsVO.getLink())
                .link(newsVO.getSummary())
                .publishedAt(newsVO.getPublishedAt())
                .build();
    }
}
