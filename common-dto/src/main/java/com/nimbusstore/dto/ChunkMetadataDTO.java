package com.nimbusstore.dto;

import lombok.*;
import jakarta.validation.constraints.NotNull;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkMetadataDTO {
    private Long id;
    private Integer chunkIndex;
    private String storageNodeId;
    private String checksum;
    private Long fileId;
    @NotNull
    @NonNull
    private StorageStatus status;
}
