package com.nimbusstore.dto;

import lombok.*;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkMetadataDTO {
    private UUID id;
    private Integer chunkIndex;
    private String storageNodeId;
    private String checksum;
    private UUID fileId;
    private StorageStatus status;
}
