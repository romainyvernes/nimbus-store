package com.nimbusstore.metadata.model;

import jakarta.persistence.*;
import lombok.*;
import com.nimbusstore.dto.StorageStatus;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer chunkIndex;
    private String storageNodeId;
    private String checksum;
    private Long fileId;

    @Enumerated(EnumType.STRING)
    private StorageStatus status;

    public ChunkMetadata(Integer chunkIndex, String storageNodeId, String checksum, Long fileId) {
        this.chunkIndex = chunkIndex;
        this.storageNodeId = storageNodeId;
        this.checksum = checksum;
        this.fileId = fileId;
        this.status = StorageStatus.PENDING;
    }
}
