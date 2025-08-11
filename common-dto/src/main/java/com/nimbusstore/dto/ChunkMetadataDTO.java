package com.nimbusstore.dto;

import lombok.*;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    @NonNull
    private StorageStatus status;
}
