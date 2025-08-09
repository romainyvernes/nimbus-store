package com.nimbusstore.metadata.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkMetadata {
    private static final String DEFAULT_STATUS = "pending";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer chunkIndex;
    private String storageNodeId;
    private String checksum;
    private Long fileId;
    private String status;

    public ChunkMetadata(Integer chunkIndex, String storageNodeId, String checksum, Long fileId) {
        this.chunkIndex = chunkIndex;
        this.storageNodeId = storageNodeId;
        this.checksum = checksum;
        this.fileId = fileId;
        this.status = DEFAULT_STATUS;
    }
}
