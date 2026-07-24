package com.openbake.drop.presentation;


import com.openbake.common.response.ApiResponse;
import com.openbake.drop.application.DropLockFacade;
import com.openbake.drop.presentation.dto.DropReserveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drops")
@RequiredArgsConstructor
public class DropLockController {

    private final DropLockFacade dropLockFacade;

    @PostMapping("/{dropId}/lock-start")
    public ApiResponse<?> reserveStock(@PathVariable("dropId") Long dropId, @RequestBody DropReserveRequest request
//                                    , @AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        Long memberId = 999L; // 테스트 용
        dropLockFacade.reserveStock(dropId, memberId, request.getQuantity());

        return ApiResponse.ok("재고 선점 및 장바구니 담기 완료");
    }
}
