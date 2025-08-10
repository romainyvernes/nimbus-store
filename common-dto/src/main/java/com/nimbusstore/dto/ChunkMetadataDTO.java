package com.nimbusstore.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkMetadataDTO {
    private Long id;
    private Integer chunkIndex;
    private String storageNodeId;
    private String checksum;
    private Long fileId;
    private StorageStatus status;
}
