package com.openbake.drop.infrastructure;

import com.openbake.drop.domain.Drop;
import com.openbake.drop.domain.DropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DropRepositoryAdapter implements DropRepository {
    private final DropJpaRepository dropJpaRepository;

    @Override
    public Drop save(Drop drop) {
        return dropJpaRepository.save(drop);
    }
}
