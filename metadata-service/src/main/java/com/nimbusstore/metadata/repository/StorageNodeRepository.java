package com.nimbusstore.metadata.repository;

import com.nimbusstore.metadata.model.StorageNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StorageNodeRepository extends JpaRepository<StorageNode, UUID> {
}
