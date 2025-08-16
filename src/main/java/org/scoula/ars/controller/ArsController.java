package org.scoula.ars.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.ars.dto.ArsDTO;
import org.scoula.ars.service.ArsService;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.security.account.domain.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"ars API"}, description="ars Controller")
@RequestMapping("/api/ars")
public class ArsController {

    private final ArsService arsService;

    @ApiOperation(value="ARS정보 업데이트", notes="ARS에서 받은 정보들 테이블에 넣습니다.")
    @PutMapping("/updateArs")
    public ResponseEntity<CommonResponseDTO<String>> updateArs(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ArsDTO arsDTO) {
        arsService.updateArs(userDetails.getUserId(),arsDTO);
        return ResponseEntity.ok(CommonResponseDTO.success("ARS정보 삽입 성공"));
    }

    @ApiOperation(value="ARS정보 조회", notes="유저의 실명, 전화번호, 생년월일을 조회합니다.")
    @GetMapping("/selectArs")
    public ResponseEntity<CommonResponseDTO<ArsDTO>> selectArs(@AuthenticationPrincipal CustomUserDetails userDetails) {
        ArsDTO dto=arsService.getArs(userDetails.getUserId());
        return ResponseEntity.ok(CommonResponseDTO.success("ARS정보 삽입 성공",dto));
    }
}
