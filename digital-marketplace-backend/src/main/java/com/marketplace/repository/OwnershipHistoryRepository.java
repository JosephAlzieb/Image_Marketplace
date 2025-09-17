package com.marketplace.repository;

import com.marketplace.model.entity.OwnershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OwnershipHistoryRepository extends JpaRepository<OwnershipHistory, UUID> {
}
