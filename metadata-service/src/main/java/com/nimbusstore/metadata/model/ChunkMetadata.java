package com.nimbusstore.metadata.model;

import jakarta.persistence.*;
import lombok.*;
import com.nimbusstore.dto.StorageStatus;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ChunkMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NonNull
    private Integer chunkIndex;
    @NonNull
    private UUID storageNodeId;
    @NonNull
    private String checksum;
    @NonNull
    private UUID fileId;

    @Enumerated(EnumType.STRING)
    private StorageStatus status = StorageStatus.PENDING;
}
