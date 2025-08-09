package com.nimbusstore.metadata.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    private static final String DEFAULT_STATUS = "pending";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String filepath;
    private Long size;
    private Integer chunkCount;
    private Integer replicationFactor;
    private String status;
    private String checksum;

    @Column(name = "created_at")
    private Instant createdAt;

    public FileMetadata(String filename, String filepath, Long size, Integer chunkCount, Integer replicationFactor, String checksum) {
        this.filename = filename;
        this.filepath = filepath;
        this.size = size;
        this.chunkCount = chunkCount;
        this.replicationFactor = replicationFactor;
        this.status = DEFAULT_STATUS;
        this.checksum = checksum;
        this.createdAt = Instant.now();
    }
}
