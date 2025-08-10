package com.nimbusstore.metadata.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import com.nimbusstore.dto.StorageStatus;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private Long size;
    private Integer chunkCount;
    private Integer replicationFactor;

    @Enumerated(EnumType.STRING)
    private StorageStatus status;
    private String checksum;

    @Column(name = "created_at")
    private Instant createdAt;

    public FileMetadata(String filename, Long size, Integer chunkCount, Integer replicationFactor, String checksum) {
        this.filename = filename;
        this.size = size;
        this.chunkCount = chunkCount;
        this.replicationFactor = replicationFactor;
        this.status = StorageStatus.PENDING;
        this.checksum = checksum;
        this.createdAt = Instant.now();
    }
}
