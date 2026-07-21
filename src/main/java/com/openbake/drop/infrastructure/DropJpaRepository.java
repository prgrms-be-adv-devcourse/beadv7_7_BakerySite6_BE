package com.openbake.drop.infrastructure;

import com.openbake.drop.domain.Drop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DropJpaRepository extends JpaRepository<Drop, Long> {
}
