package org.scoula.dictionary.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.dictionary.dto.DictionaryDTO;
import org.scoula.dictionary.service.DictionaryService;
import org.scoula.quiz.dto.QuizDTO;
import org.scoula.quiz.dto.QuizHistoryDTO;
import org.scoula.quiz.dto.QuizHistoryDetailDTO;
import org.scoula.quiz.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"금융사전API"}, description="Dictionary Cotroller")
@RequestMapping("/api/dictionary")
public class DictionaryController {

    final private DictionaryService dictionaryService;


    @ApiOperation(value = "금융사전 전체조회", notes = "금융사전에 등록된 단어 전체를 조회합니다.")
    @GetMapping("")
    public ResponseEntity<CommonResponseDTO<List<DictionaryDTO>>> getList() {
        List<DictionaryDTO> wordList = dictionaryService.getList();
        return ResponseEntity.ok(CommonResponseDTO.success("단어 전체 조회 성공", wordList));
    }

    @ApiOperation(value = "금융사전 상세조회", notes = "금융사전에 등록된 단어를 상세 조회합니다.")
    @GetMapping("/id={dictionaryId}")
    public ResponseEntity<CommonResponseDTO<DictionaryDTO>> getDetail(@PathVariable int dictionaryId) {
        DictionaryDTO word = dictionaryService.getDetail(dictionaryId);
        return ResponseEntity.ok(CommonResponseDTO.success("단어 상세 조회 성공", word));
    }

    @ApiOperation(value = "금융사전 단어검색조회", notes = "사용자가 단어를 검색하여 조회합니다.")
    @GetMapping("/word={keyword}")
    public ResponseEntity<CommonResponseDTO<List<DictionaryDTO>>> getSearch(@PathVariable String keyword) {
        List<DictionaryDTO> word = dictionaryService.getBySearch(keyword);
        return ResponseEntity.ok(CommonResponseDTO.success("단어 상세 조회 성공", word));
    }

}
