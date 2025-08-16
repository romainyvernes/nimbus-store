package com.nimbusstore.dto;

import lombok.*;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataDTO {
    private UUID id;
    private String filename;
    private Long size;
    private Integer chunkCount;
    private StorageStatus status;
    private String checksum;
}
