package com.nimbusstore.metadata.service;

import com.nimbusstore.metadata.model.ChunkMetadata;
import com.nimbusstore.metadata.model.StorageNode;
import com.nimbusstore.metadata.repository.StorageNodeRepository;
import com.nimbusstore.metadata.repository.ChunkRepository;
import com.nimbusstore.dto.ReplicationRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ChunkReplicationService {

    private final StorageNodeRepository nodeRepo;
    private final ChunkRepository chunkRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    public ChunkReplicationService(StorageNodeRepository nodeRepo, ChunkRepository chunkRepo) {
        this.nodeRepo = nodeRepo;
        this.chunkRepo = chunkRepo;
    }

    public boolean replicateChunk(ChunkMetadata chunk, String deadNodeId) {
        // Find all node IDs that already have this chunk
        List<ChunkMetadata> replicas = chunkRepo.findByChecksum(chunk.getChecksum());
        Set<String> existingNodeIds = replicas.stream()
            .map(ChunkMetadata::getStorageNodeId)
            .collect(Collectors.toSet());

        // Find a healthy node not already storing the chunk
        List<StorageNode> healthyNodes = nodeRepo.findAll().stream()
            .filter(node -> !existingNodeIds.contains(node.getId().toString()))
            .toList();

        if (healthyNodes.isEmpty()) {
            // No available node for replication
            return false;
        }

        StorageNode targetNode = healthyNodes.get(
            ThreadLocalRandom.current().nextInt(healthyNodes.size())
        );

        String sourceNodeId = replicas.stream()
            .map(ChunkMetadata::getStorageNodeId)
            .filter(storageNodeId -> !storageNodeId.equals(deadNodeId))
            .findFirst()
            .orElse(null);

        StorageNode sourceNode = (sourceNodeId != null)
            ? nodeRepo.findById(UUID.fromString(sourceNodeId)).orElse(null)
            : null;

        if (sourceNode == null) {
            return false;
        }

        // Send a request to the source node to replicate the chunk to the target node
        String replicateUrl = sourceNode.getUrl() + "/replicate";
        ReplicationRequestDTO payload = new ReplicationRequestDTO(chunk.getChecksum(), targetNode.getUrl());
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(replicateUrl, payload, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        // Update chunk metadata to reflect new storage node
        chunk.setStorageNodeId(targetNode.getId().toString());
        chunkRepo.save(chunk);
        return true;
    }
}
