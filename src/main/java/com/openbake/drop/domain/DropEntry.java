package com.openbake.drop.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "drop_entries")
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 적용
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DropEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dropEntryId;

    @Column(nullable = false)
    private Long dropId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EntryStatus entryStatus; // 진입 상태

    @CreatedDate // JPA (엔티티 저장 시점에 JPA가 자동으로 시간 주입)
    @Column(nullable = false, updatable = false)
    private LocalDateTime entryTime; // 진입 시간 (선착순 판정의 기준)

    @Builder
    public DropEntry(Long dropId, Long memberId, EntryStatus entryStatus) {
        if (dropId == null || memberId == null || entryStatus == null) {
            throw new IllegalArgumentException("드롭 ID, 회원 ID, 진입 상태는 필수입니다.");
        }

        this.dropId = dropId;
        this.memberId = memberId;
        this.entryStatus = entryStatus;
        this.entryTime = LocalDateTime.now();
    }
}
