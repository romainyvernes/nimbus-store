package com.nimbusstore.dto;

import lombok.*;
import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataDTO {
    private Long id;
    private String filename;
    private String filepath;
    private Long size;
    private Integer chunkCount;
    private Integer replicationFactor;
    private StorageStatus status;
    private String checksum;
}
