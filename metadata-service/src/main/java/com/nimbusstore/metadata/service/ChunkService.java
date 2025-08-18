package com.nimbusstore.metadata.service;

import com.nimbusstore.metadata.model.ChunkMetadata;
import com.nimbusstore.metadata.model.StorageNode;
import com.nimbusstore.metadata.repository.ChunkRepository;
import com.nimbusstore.metadata.repository.StorageNodeRepository;
import com.nimbusstore.dto.StorageStatus;
import com.nimbusstore.dto.ChunkMetadataDTO;
import com.nimbusstore.metadata.mapper.ChunkMetadataMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.UUID;

@Service
public class ChunkService {

    private final ChunkRepository repo;
    private final StorageNodeRepository nodeRepo;
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
    private final ChunkMetadataMapper mapper;

    @Value("${replication-factor}")
    private int replicationFactor;

    public ChunkService(ChunkRepository repo, StorageNodeRepository nodeRepo, ChunkMetadataMapper mapper) {
        this.repo = repo;
        this.nodeRepo = nodeRepo;
        this.mapper = mapper;
    }

    private UUID getNextNodeId() {
        List<StorageNode> nodes = nodeRepo.findAll();
        if (nodes.isEmpty()) {
            return null;
        }
        int idx = roundRobinIndex.getAndUpdate(i -> (i + 1) % nodes.size());
        return nodes.get(idx).getId();
    }

    public void updateStatus(UUID id, StorageStatus status) {
        try {
            ChunkMetadata chunk = repo.findById(id).orElse(null);
            if (chunk != null) {
                chunk.setStatus(status);
                repo.save(chunk);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public ChunkMetadataDTO toDtoWithNodeUrl(ChunkMetadata chunk) {
        ChunkMetadataDTO dto = mapper.toDto(chunk);
        nodeRepo.findById(chunk.getStorageNodeId())
            .ifPresent(node -> dto.setNodeUrl(node.getUrl()));
        return dto;
    }

    /**
     * Replicates a chunk up to the configured replication factor, using round-robin node assignment.
     * Returns a list of all replicas (existing + newly created).
     */
    public List<ChunkMetadataDTO> replicateChunk(ChunkMetadataDTO chunkDto) {
        List<ChunkMetadata> existingReplicas = repo.findByChecksum(chunkDto.getChecksum());
        int currentReplicaCount = existingReplicas.size();

        List<ChunkMetadataDTO> resultDtos = new ArrayList<>();

        // If we already have enough replicas, return them
        if (currentReplicaCount >= replicationFactor) {
            for (ChunkMetadata replica : existingReplicas) {
                resultDtos.add(toDtoWithNodeUrl(replica));
            }
            return resultDtos;
        }

        // Otherwise, create new replicas up to replicationFactor
        int needed = replicationFactor - currentReplicaCount;
        for (int i = 0; i < needed; i++) {
            ChunkMetadata chunk = mapper.toEntity(chunkDto);
            UUID assignedNode = getNextNodeId();
            if (assignedNode == null) continue;
            chunk.setStorageNodeId(assignedNode);
            repo.save(chunk);
            resultDtos.add(toDtoWithNodeUrl(chunk));
        }

        // Add existing replicas to the result
        for (ChunkMetadata replica : existingReplicas) {
            resultDtos.add(toDtoWithNodeUrl(replica));
        }

        return resultDtos.isEmpty() ? null : resultDtos;
    }
}
