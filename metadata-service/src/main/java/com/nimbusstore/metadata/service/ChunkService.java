package com.nimbusstore.metadata.service;

import com.nimbusstore.metadata.model.ChunkMetadata;
import com.nimbusstore.metadata.model.StorageNode;
import com.nimbusstore.metadata.repository.ChunkRepository;
import com.nimbusstore.metadata.repository.StorageNodeRepository;
import com.nimbusstore.dto.StorageStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.UUID;

@Service
public class ChunkService {

    private final ChunkRepository repo;
    private final StorageNodeRepository nodeRepo;
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    public ChunkService(ChunkRepository repo, StorageNodeRepository nodeRepo) {
        this.repo = repo;
        this.nodeRepo = nodeRepo;
    }

    public ChunkMetadata save(ChunkMetadata chunk) {
        String assignedNode = getNextNodeId();
        chunk.setStorageNodeId(assignedNode);
        return repo.save(chunk);
    }

    private String getNextNodeId() {
        List<StorageNode> nodes = nodeRepo.findAll();
        if (nodes.isEmpty()) {
            throw new IllegalStateException("No storage nodes available");
        }
        int idx = roundRobinIndex.getAndUpdate(i -> (i + 1) % nodes.size());
        return nodes.get(idx).getId().toString();
    }

    public Optional<ChunkMetadata> findById(UUID id) {
        return repo.findById(id);
    }

    public ChunkMetadata updateStatus(UUID id, StorageStatus status) {
        ChunkMetadata chunk = repo.findById(id).orElseThrow();
        chunk.setStatus(status);
        return repo.save(chunk);
    }
}
