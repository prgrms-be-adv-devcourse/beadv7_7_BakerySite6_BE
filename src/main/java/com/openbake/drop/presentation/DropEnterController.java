package com.openbake.drop.presentation;

import com.openbake.common.response.ApiResponse;
import com.openbake.drop.application.DropEnterService;
import com.openbake.drop.application.DropService;
import com.openbake.drop.application.dto.ConfirmEntryResponse;
import com.openbake.drop.application.dto.QueueRankResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drops")
@RequiredArgsConstructor
public class DropEnterController {

    private final DropEnterService dropEnterService;

    @PostMapping("/{dropId}/confirm-entry")
    public ApiResponse<ConfirmEntryResponse> enterDrop(@PathVariable("dropId") Long dropId
//                                    ,@AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        Long memberId = 999L; // 로컬 테스트 용
        // sellerId 도 받아와야함.
        ConfirmEntryResponse response = dropEnterService.confirmEntry(dropId, memberId);
        return ApiResponse.ok(response);
    }


    @PostMapping("/{dropId}/enter") // 대기열 진입 할 때
    public ApiResponse<QueueRankResponse> enterQueue(@PathVariable("dropId") Long dropId
//                                    ,@AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
//        Long memberId = customUserDetails.getMemberId();
        // sellerId 도 받아와야함.
        Long memberId = 999L; // 로컬 테스트 용
        QueueRankResponse response = dropEnterService.enterQueue(dropId, memberId);
        return ApiResponse.ok(response);
    }

    @GetMapping("/{dropId}/queue/rank") // 내 대기열 순번 확인 (프론트에서 지속적으로 호출)
    public ApiResponse<QueueRankResponse> getQueueRank(@PathVariable("dropId") Long dropId
//                                       ,@AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        Long memberId = 999L; // 테스트 값
        // sellerId 도 받아와야함.
        QueueRankResponse response = dropEnterService.getRank(dropId, memberId);
        return ApiResponse.ok(response);
    }

    @GetMapping("/today/drop") // 오늘 진행하는 드롭ID 가져오기 (대기열 선행 작업, ID가 있어야 대기열 생성가능)
    public ApiResponse<?> getDropId(){
        Long dropId = dropEnterService.getTodayDropId();
        return ApiResponse.ok(dropId);
    }
}
