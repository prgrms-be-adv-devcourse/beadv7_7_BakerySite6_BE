package com.openbake.drop.application;

import com.openbake.drop.domain.*;
import com.openbake.drop.presentation.dto.DropProductInfoRequest;
import com.openbake.drop.presentation.dto.DropProductInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DropService {

    private final DropRepository dropRepository;
    private final DropInventoryRepository dropInventoryRepository;

    @Transactional
    public DropProductInfoResponse registerDropProduct(DropProductInfoRequest request, Long sellerId) {
        DropProduct dropProduct = createDropProduct(request);

        Drop drop = createDrop(request, dropProduct, sellerId);

        Drop savedDrop = dropRepository.save(drop);

        DropInventory dropInventory = createDropInventory(savedDrop, request);

        DropInventory savedDropInventory = dropInventoryRepository.save(dropInventory);

        return DropProductInfoResponse.of(savedDrop, savedDropInventory);
    }


    private DropInventory createDropInventory(Drop savedDrop, DropProductInfoRequest dropProductInfoRequest) {
        return DropInventory.builder()
                .dropId(savedDrop.getId())
                .totalQuantity(dropProductInfoRequest.totalQuantity())
                .remainQuantity(dropProductInfoRequest.totalQuantity()) // 처음에는 총 수량 = 남은 수량
                .build();
    }


    private Drop createDrop(DropProductInfoRequest dropProductInfoRequest, DropProduct dropProduct, Long sellerId) {
        return Drop.builder()
                .dropStatus(DropStatus.UPCOMING)
                .pickUpAvailableDates(dropProductInfoRequest.pickUpAvailableDates())
                .dropProduct(dropProduct)
                .limitQuantity(dropProductInfoRequest.limitQuantity())
                .dropStart(dropProductInfoRequest.dropStart())
                .dropEnd(dropProductInfoRequest.dropEnd())
                .sellerId(sellerId)
                .build();
    }

    private DropProduct createDropProduct(DropProductInfoRequest dropProductInfoRequest) {
        return DropProduct.builder()
                .name(dropProductInfoRequest.name())
                .description(dropProductInfoRequest.description())
                .imageUrl(dropProductInfoRequest.imageUrl())
                .price(dropProductInfoRequest.price())
                .build();
    }

}
