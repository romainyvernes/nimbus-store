package com.nimbusstore.metadata.repository;

import com.nimbusstore.metadata.model.ChunkMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<ChunkMetadata, Long> {
}

