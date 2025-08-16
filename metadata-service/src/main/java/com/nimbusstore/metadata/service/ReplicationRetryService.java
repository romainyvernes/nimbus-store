package com.nimbusstore.metadata.service;

import com.nimbusstore.dto.StorageStatus;
import com.nimbusstore.metadata.model.FailedReplication;
import com.nimbusstore.metadata.model.ChunkMetadata;
import com.nimbusstore.metadata.repository.FailedReplicationRepository;
import com.nimbusstore.metadata.repository.ChunkRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Service
public class ReplicationRetryService {

    private final FailedReplicationRepository failedReplicationRepo;
    private final ChunkRepository chunkRepo;
    private final ChunkReplicationService chunkReplicationService;

    @Value("${replication-factor}")
    private int replicationFactor;

    public ReplicationRetryService(
            FailedReplicationRepository failedReplicationRepo,
            ChunkRepository chunkRepo,
            ChunkReplicationService chunkReplicationService) {
        this.failedReplicationRepo = failedReplicationRepo;
        this.chunkRepo = chunkRepo;
        this.chunkReplicationService = chunkReplicationService;
    }

    @Scheduled(fixedRateString = "${replication.retry.frequency}")
    @Transactional
    public void retryFailedReplications() {
        List<FailedReplication> failedReplications = failedReplicationRepo.findAll();
        for (FailedReplication failed : failedReplications) {
            ChunkMetadata chunk = chunkRepo.findById(failed.getChunkId()).orElse(null);
            if (chunk != null) {
                // Only replicate if current replicas < replicationFactor
                List<ChunkMetadata> replicas = chunkRepo.findByChecksum(chunk.getChecksum());
                if (replicas.size() < replicationFactor) {
                    // Set status to REPLICATING before starting replication
                    chunk.setStatus(StorageStatus.REPLICATING);
                    chunkRepo.save(chunk);

                    boolean success = chunkReplicationService.replicateChunk(chunk, failed.getFailedNodeId());

                    // Update status based on replication result
                    chunk.setStatus(success ? StorageStatus.COMPLETED : StorageStatus.FAILED);
                    chunkRepo.save(chunk);

                    if (success) {
                        failedReplicationRepo.delete(failed);
                    }
                }
            } else {
                // Remove failed replication if chunk no longer exists
                failedReplicationRepo.delete(failed);
            }
        }
    }
}
