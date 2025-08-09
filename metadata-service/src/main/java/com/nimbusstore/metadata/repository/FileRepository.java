package com.nimbusstore.metadata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nimbusstore.metadata.model.FileMetadata;

public interface FileRepository extends JpaRepository<FileMetadata, Long> {
}
