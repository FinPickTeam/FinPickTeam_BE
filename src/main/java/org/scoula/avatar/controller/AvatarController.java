package org.scoula.avatar.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.avatar.dto.AvatarDTO;
import org.scoula.avatar.dto.UserClothesDTO;
import org.scoula.avatar.service.AvatarService;
import org.scoula.common.dto.CommonResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"아바타API"}, description="Avatar Controller")
@RequestMapping("/api/avatar")
public class AvatarController {

    final private AvatarService avatarService;

    //아바타 생성
    //swagger 테스트용
    @ApiOperation(value="아바타 생성", notes="아바타를 생성합니다.")
    @PostMapping("/userId={userId}")
    public ResponseEntity<CommonResponseDTO<String>> insertAvatar(@PathVariable Long userId) {
        avatarService.insertAvatar(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("아바타 생성 성공"));
    }


    //아바타 착장 조회(get, 파라미터 : Long userId, 반환값 : AvatarDTO)
    @ApiOperation(value="아바타 조회", notes="아바타 상태를 조회합니다.")
    @GetMapping("/userId={userId}")
    public ResponseEntity<CommonResponseDTO<AvatarDTO>> getAvatar(@PathVariable Long userId) {
        AvatarDTO avatar = avatarService.getAvatar(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("아바타 조회성공", avatar));
    }

    //아바타 착장 수정(put, 파라미터 : Long userId,Long[] items, 반환값 : 성공여부)
    //각 아이템들마다 해당하는 타입파악(select type from item where id=#{itemId}) 후, 해당하는 컬럼에 각각 반영
    @ApiOperation(value="아바타 수정", notes = "아바타 상태를 수정합니다.")
    @PutMapping("/updateAvatar")
    public ResponseEntity<CommonResponseDTO<String>> updateAvatar(@RequestParam Long userId, Long[] items) {
        avatarService.updateAvatar(userId, items);
        return ResponseEntity.ok(CommonResponseDTO.success("아바타 수정성공"));
    }

    //의상 전체 조회(get, 파라미터 : Long userId, 반환값 : List<ItemDTO>)
    @ApiOperation(value="의상 전체 조회", notes="의상 전체를 조회합니다. 소유여부도 is_wearing으로 표시합니다.")
    @GetMapping("/getClothes/userId={userId}")
    public ResponseEntity<CommonResponseDTO<List<UserClothesDTO>>> getClothes(@PathVariable Long userId) {
        List<UserClothesDTO> userClothes=  avatarService.getUserClothes(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("의상 전체 조회성공", userClothes));
    }

    //구매처리(post, 파라미터 : Long userId, int itemId, 반환값 : 성공여부)
    //아이템 cost 검색하여 유저의 보유재화와 비교, 구매가능한지 판단
    // -> 가능할 경우 cost만큼 보유재화 차감하고, coin_history에 소비내역 추가, clothes 테이블에 해당 아이템 삽입
    @ApiOperation(value="의상 구매", notes="유저 옷장에 해당 아이템을 넣고 재화를 차감합니다.")
    @PostMapping("/insertClothe")
    public ResponseEntity<CommonResponseDTO<String>> insertClothe(@RequestParam Long userId, Long itemId){
        avatarService.insertClothe(userId, itemId);
        return ResponseEntity.ok(CommonResponseDTO.success("의상 구매성공"));
    }

    @ApiOperation(value="유저재화조회", notes="현재 유저가 보유 중인 재화를 조회합니다.")
    @GetMapping("/getCurCoin/userId={userId}")
    public ResponseEntity<CommonResponseDTO<Integer>> getCurCoin(@PathVariable Long userId) {
        int curCoin=avatarService.getCoin(userId);
        return ResponseEntity.ok(CommonResponseDTO.success("유저재화 조회 성공", curCoin));
    }
}
