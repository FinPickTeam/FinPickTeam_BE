package org.scoula.news.controller;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.news.dto.NewsDTO;
import org.scoula.news.service.NewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("api/news")

public class NewsController {

    final private NewsService newsService;


    @ApiOperation(value = "네이버뉴스 삽입하기", notes = "네이버API를 이용해 \"금융\" 키워드를 정확도에 따라서 검색하고, DB에 저장합니다..")
    @PostMapping("")
    public ResponseEntity<CommonResponseDTO<String>> insertNews() {
        newsService.insertNews();
        return ResponseEntity.ok(CommonResponseDTO.success("네이버 뉴스 삽입 성공"));
    }

    //추후 새벽 2시마다 뉴스DB목록 갱신하는 스케쥴링 필요
    @ApiOperation(value = "뉴스목록 조회하기", notes = "DB에 저장된 뉴스들을 가져옵니다.")
    @GetMapping("/SearchNews")
    public ResponseEntity<CommonResponseDTO<List<NewsDTO>>> getNewsList() {
        List<NewsDTO> newsDTO= newsService.getList();
        return ResponseEntity.ok(CommonResponseDTO.success("뉴스 조회 성공",newsDTO));
    }

}