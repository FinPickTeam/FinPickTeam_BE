package org.scoula.news.service;

import org.scoula.news.dto.NewsDTO;

import java.util.List;

public interface NewsService {

    void insertNews();

    List<NewsDTO> getList();
}
