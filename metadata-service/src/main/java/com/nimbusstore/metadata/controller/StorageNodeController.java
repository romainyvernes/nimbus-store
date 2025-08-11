package com.nimbusstore.metadata.controller;

import com.nimbusstore.metadata.model.StorageNode;
import com.nimbusstore.metadata.repository.StorageNodeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/nodes")
public class StorageNodeController {

    private final StorageNodeRepository repo;

    public StorageNodeController(StorageNodeRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public ResponseEntity<StorageNode> createNode() {
        StorageNode saved = repo.save(new StorageNode());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable UUID id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

