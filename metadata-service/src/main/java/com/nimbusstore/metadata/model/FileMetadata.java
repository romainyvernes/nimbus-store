package com.nimbusstore.metadata.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import com.nimbusstore.dto.StorageStatus;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NonNull
    private String filename;
    @NonNull
    private Long size;
    @NonNull
    private Integer chunkCount;
    @NonNull
    private String checksum;

    @Enumerated(EnumType.STRING)
    private StorageStatus status = StorageStatus.PENDING;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
