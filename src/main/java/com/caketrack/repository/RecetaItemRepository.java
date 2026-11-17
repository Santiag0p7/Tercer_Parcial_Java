package com.caketrack.repository;

import com.caketrack.model.RecetaItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecetaItemRepository extends JpaRepository<RecetaItem, Long> {
}
