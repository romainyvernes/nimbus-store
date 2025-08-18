package com.nimbusstore.dto;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChunkMetadataDTO {
    private UUID id;
    @NotNull
    private Integer chunkIndex;
    private String nodeUrl;
    @NotNull
    private String checksum;
    @NotNull
    private UUID fileId;
    private StorageStatus status;
}
