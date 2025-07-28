package org.scoula.bubble.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.bubble.dto.BubbleDTO;
import org.scoula.bubble.service.BubbleService;
import org.scoula.common.dto.CommonResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"말풍선API"}, description="Bubble Controller")
@RequestMapping("/api/bubble")
public class BubbleContoller {
    final private BubbleService bubbleService;

    @ApiOperation(value = "말풍선 조회", notes = "말풍선을 랜덤으로 제공합니다.")
    @GetMapping("")
    public ResponseEntity<CommonResponseDTO<BubbleDTO>> getQuiz() {
        BubbleDTO bubble = bubbleService.getBubble();
        return ResponseEntity.ok(CommonResponseDTO.success("말풍선 조회 성공", bubble));
    }
}
