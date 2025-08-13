package com.nimbusstore.metadata.controller;

import com.nimbusstore.metadata.model.StorageNode;
import com.nimbusstore.metadata.repository.StorageNodeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/nodes")
public class StorageNodeController {

    private final StorageNodeRepository repo;

    public StorageNodeController(StorageNodeRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> createNode(@RequestBody Map<String, UUID> body) {
        StorageNode node = new StorageNode(body.get("id"));
        repo.save(node);
        return ResponseEntity.ok().build();
    }
}
