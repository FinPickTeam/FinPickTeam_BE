package org.scoula.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.alarm.service.AlarmService;
import org.scoula.news.domain.NewsVO;
import org.scoula.news.dto.NaverNewsResponseDTO;
import org.scoula.news.dto.NewsDTO;
import org.scoula.news.mapper.NewsMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpHeaders;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
@PropertySource({"classpath:/application.properties"})
public class NewsServiceImpl implements NewsService {

    final private NewsMapper newsMapper;
    final private AlarmService alarmService;
    final private RestTemplate restTemplate=new RestTemplate();

    @Value("${naver.api.client-id}")
    private String id;
    @Value("${naver.api.client-secret}")
    private String SECRET;
    @Value("${naver.api.news-url}")
    private String URL;


    @Transactional
    @Override
    public void insertNews() {

        //http요청 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", id);
        headers.set("X-Naver-Client-Secret", SECRET);
        headers.setAccept(List.of(new MediaType("application", "json", StandardCharsets.UTF_8)));


        String keyword = "금융";

        URI uri = UriComponentsBuilder.fromHttpUrl(URL)
                .queryParam("query", keyword) // <--- 인코딩되지 않은 원본 'keyword' 변수를 그대로 전달
                .queryParam("start", 1)
                .queryParam("display", 10)
                .queryParam("sort", "sim")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        log.info("Request URI: " + uri);

        //httpEntity 요청 생성 및 헤더삽입
        HttpEntity<String> entity = new HttpEntity<>(headers);

        //api 요청 후, 네이버뉴스기사 받음.
        NaverNewsResponseDTO naverNewsResponseDTO = restTemplate.exchange( //헤더 삽입이 불가능한 getForObject 대신 exchange 사용
                uri, // 요청 URL
                HttpMethod.GET,
                entity,
                NaverNewsResponseDTO.class
        ).getBody();

        if(naverNewsResponseDTO==null){
            throw new NullPointerException("네이버 뉴스 API 응답이 null입니다.");
        }

        //new
        List<NewsVO> newsVOList = naverNewsResponseDTO.toVO();

        if (!newsVOList.isEmpty()) { // 삽입할 데이터가 있을 경우에만 호출
            newsMapper.insertNews(newsVOList);
        }

        //뉴스 생성 시 알림추가
        alarmService.addAlarmAll("새로운 뉴스가 도착했습니다. 확인해보세요!");
    }

    @Override
    public List<NewsDTO> getList() {
        List<NewsVO> newsVOList = newsMapper.getAllNews();

        List<NewsDTO> newsDTOs = new ArrayList<>();
        for (NewsVO newsVO : newsVOList) {
            NewsDTO newsDTO = NewsDTO.of(newsVO);
            newsDTOs.add(newsDTO);
        }

        return newsDTOs;
    }
}
