package com.openbake.drop.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Getter
@Table(name = "drops")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Drop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DropStatus dropStatus; // 드롭 상태 (시작 전, 진행 중, 마감)

    @Embedded
    private DropProduct dropProduct; // 드롭 상품 VO

    @Column(nullable = false)
    private int limitQuantity; // 1인당 한정 수량

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "pickup_available_dates",
            joinColumns = @JoinColumn(name = "drop_id")
    )
    @Column(name = "available_date", nullable = false)
    private Set<LocalDate> pickUpAvailableDate = new HashSet<>(); // 픽업 가능 날짜

    @Column(nullable = false)
    private LocalDateTime dropStart; // 드롭 시작 시간

    @Column(nullable = false)
    private LocalDateTime dropEnd; // 드롭 마감 시간

    @Column(nullable = false)
    private Long sellerId; // 판매자 ID

    @Builder
    public Drop(DropStatus dropStatus, DropProduct dropProduct, Set<LocalDate> pickUpAvailableDates, int limitQuantity, LocalDateTime dropStart, LocalDateTime dropEnd, Long sellerId) {
        validateDropPeriod(dropStart, dropEnd);
        validatePickUpDates(dropEnd, pickUpAvailableDates);
        if (limitQuantity <= 0) {
            throw new IllegalArgumentException("1인당 제한 수량은 1개 이상이어야 합니다.");
        }
        if (sellerId == null || dropProduct == null || dropStatus == null) {
            throw new IllegalArgumentException("판매자 ID, 상품 정보, 드롭 상태는 필수입니다.");
        }


        this.dropStatus = dropStatus;
        this.dropProduct = dropProduct;
        this.limitQuantity = limitQuantity;
        this.dropStart = dropStart;
        this.dropEnd = dropEnd;
        this.sellerId = sellerId;

        if (pickUpAvailableDates != null) {
            this.pickUpAvailableDate.addAll(pickUpAvailableDates);
        }
    }

    private void validateDropPeriod(LocalDateTime dropStart, LocalDateTime dropEnd) {
        if (dropStart == null || dropEnd == null) {
            throw new IllegalArgumentException("시작 시간과 마감 시간은 필수입니다.");
        }
        if (!dropStart.isBefore(dropEnd)) {
            throw new IllegalArgumentException("시작 시간은 마감 시간보다 이전이어야 합니다.");
        }
    }

    private void validatePickUpDates(LocalDateTime dropEnd, Set<LocalDate> pickUpAvailableDates) {
        if (pickUpAvailableDates == null || pickUpAvailableDates.isEmpty()) {
            throw new IllegalArgumentException("픽업 가능 날짜는 최소 하루 이상 필요합니다.");
        }

        LocalDate dropEndDate = dropEnd.toLocalDate();
        boolean hasInvalidDate = pickUpAvailableDates.stream()
                .anyMatch(date -> !date.isAfter(dropEndDate));

        if (hasInvalidDate) {
            throw new IllegalArgumentException("모든 픽업 가능 날짜는 드롭 마감일 이후여야 합니다.");
        }
    }

}