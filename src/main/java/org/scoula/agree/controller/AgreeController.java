package org.scoula.agree.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.agree.service.AgreeService;
import org.scoula.common.dto.CommonResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Log4j2
@Api(tags="약관동의API", description = "agree controller")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/agree")
public class AgreeController {

    final private AgreeService agreeService;

    @ApiOperation(value="약관동의정보 생성", notes="유저별로 약관동의와 관련한 기본정보들을 생성합니다.")
    @PostMapping("")
    public ResponseEntity<CommonResponseDTO<String>> createAgree(@RequestParam Long userId) {
        agreeService.createAgree(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("약관동의정보 생성 성공"));
    }

    @ApiOperation(value="오픈뱅킹약관 동의", notes="오픈뱅킹약관에 동의한 내용을 저장합니다.")
    @PutMapping("/openbanking")
    public ResponseEntity<CommonResponseDTO<String>> agreeOpenbanking(@RequestParam Long userId) {
        agreeService.updateOpenBankingAgree(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("오픈뱅킹약관 동의 성공"));
    }

    @ApiOperation(value="개인정보약관 동의", notes="개인정보약관에 동의한 내용을 저장합니다.")
    @PutMapping("/personalInfo")
    public ResponseEntity<CommonResponseDTO<String>> agreePersonalInfo(@RequestParam Long userId) {
        agreeService.updatePersonalInfoAgree(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("개인정보약관 동의 성공"));
    }

    @ApiOperation(value="ars 동의", notes="ars약관에 동의한 내용을 저장합니다.")
    @PutMapping("/ars")
    public ResponseEntity<CommonResponseDTO<String>> agreeArs(@RequestParam Long userId) {
        agreeService.updateArsAgree(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("ars약관 동의 성공"));
    }

}
