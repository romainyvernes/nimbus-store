package com.nimbusstore.metadata.service;

import com.nimbusstore.dto.StorageStatus;
import com.nimbusstore.metadata.model.StorageNode;
import com.nimbusstore.metadata.model.ChunkMetadata;
import com.nimbusstore.metadata.model.FailedReplication;
import com.nimbusstore.metadata.repository.StorageNodeRepository;
import com.nimbusstore.metadata.repository.ChunkRepository;
import com.nimbusstore.metadata.repository.FailedReplicationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Service
public class NodeCleanupService {

    private final StorageNodeRepository nodeRepo;
    private final ChunkRepository chunkRepo;
    private final FailedReplicationRepository failedReplicationRepo;
    private final ChunkReplicationService replicationService;

    @Value("${dead-node-threshold}")
    private long deadNodeThreshold;

    @Value("${replication-factor}")
    private int replicationFactor;

    public NodeCleanupService(StorageNodeRepository nodeRepo,
                             ChunkRepository chunkRepo,
                             FailedReplicationRepository failedReplicationRepo,
                             ChunkReplicationService replicationService) {
        this.nodeRepo = nodeRepo;
        this.chunkRepo = chunkRepo;
        this.failedReplicationRepo = failedReplicationRepo;
        this.replicationService = replicationService;
    }

    /**
     * Returns true if the node is considered dead based on lastHeartbeat and threshold.
     */
    public boolean isNodeDead(StorageNode node) {
        long now = System.currentTimeMillis();
        return now - node.getLastHeartbeat() > deadNodeThreshold;
    }

    /**
     * Returns the first live replica from the given list, or null if none are live.
     */
    public ChunkMetadata findLiveReplica(List<ChunkMetadata> replicas) {
        for (ChunkMetadata replica : replicas) {
            String nodeIdStr = replica.getStorageNodeId();
            try {
                StorageNode node = nodeRepo.findById(java.util.UUID.fromString(nodeIdStr)).orElse(null);
                if (node != null && !isNodeDead(node)) {
                    return replica;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    @Scheduled(fixedRateString = "${dead-node-threshold}")
    @Transactional
    public void sweepForDeadNodes() {
        long now = System.currentTimeMillis();
        List<StorageNode> deadNodes = nodeRepo.findAll().stream()
            .filter(this::isNodeDead)
            .toList();

        for (StorageNode deadNode : deadNodes) {
            List<ChunkMetadata> chunks = chunkRepo.findByStorageNodeId(deadNode.getId().toString());
            for (ChunkMetadata chunk : chunks) {
                List<ChunkMetadata> replicas = chunkRepo.findByChecksum(chunk.getChecksum());
                if (replicas.size() < replicationFactor) {
                    chunk.setStatus(StorageStatus.REPLICATING);
                    chunkRepo.save(chunk);

                    boolean replicated = replicationService.replicateChunk(chunk, deadNode.getId().toString());

                    chunk.setStatus(replicated ? StorageStatus.COMPLETED : StorageStatus.FAILED);
                    chunkRepo.save(chunk);

                    if (!replicated) {
                        failedReplicationRepo.save(new FailedReplication(chunk.getId(), chunk.getStorageNodeId()));
                    }
                }
            }
            nodeRepo.delete(deadNode);
        }
    }
}