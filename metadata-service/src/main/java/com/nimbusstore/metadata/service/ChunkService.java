package com.nimbusstore.metadata.service;

import com.nimbusstore.metadata.model.ChunkMetadata;
import com.nimbusstore.metadata.repository.ChunkRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChunkService {

    private final ChunkRepository repo;

    public ChunkService(ChunkRepository repo) {
        this.repo = repo;
    }

    public ChunkMetadata save(ChunkMetadata chunk) {
        return repo.save(chunk);
    }

    public Optional<ChunkMetadata> findById(Long id) {
        return repo.findById(id);
    }

    public ChunkMetadata updateStatus(Long id, String status) {
        ChunkMetadata chunk = repo.findById(id).orElseThrow();
        chunk.setStatus(status);
        return repo.save(chunk);
    }
}
