package com.openbake.drop.infrastructure;

import com.openbake.drop.domain.DropEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DropEntryJpaRepository extends JpaRepository<DropEntry, Long> {

}
