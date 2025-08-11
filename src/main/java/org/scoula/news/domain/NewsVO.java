package org.scoula.news.domain;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class NewsVO {
    private Long id;
    private String title;
    private String link;
    private String summary;
    private LocalDateTime publishedAt;
    private String keyword;
}