package com.nimbusstore.metadata.repository;

import com.nimbusstore.metadata.model.ChunkMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChunkRepository extends JpaRepository<ChunkMetadata, UUID> {
    List<ChunkMetadata> findByStorageNodeId(String storageNodeId);
    List<ChunkMetadata> findByChecksum(String checksum);
}
