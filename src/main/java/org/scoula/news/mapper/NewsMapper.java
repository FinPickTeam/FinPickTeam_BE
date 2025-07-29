package org.scoula.news.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.news.domain.NewsVO;

import java.util.List;

@Mapper
public interface NewsMapper {
    void insertNews(List<NewsVO> newsVO);
    List<NewsVO> getAllNews();
}

