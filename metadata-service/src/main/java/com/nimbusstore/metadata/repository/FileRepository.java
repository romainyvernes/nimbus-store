package com.nimbusstore.metadata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nimbusstore.metadata.model.FileMetadata;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileMetadata, UUID> {
}
