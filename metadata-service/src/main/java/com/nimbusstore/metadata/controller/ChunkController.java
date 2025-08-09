package com.nimbusstore.metadata.controller;

import com.nimbusstore.metadata.model.ChunkMetadata;
import com.nimbusstore.metadata.service.ChunkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chunks")
public class ChunkController {

    private final ChunkService service;

    public ChunkController(ChunkService service) {
        this.service = service;
    }

    // TODO: replace ChunkMetadata with a DTO for better separation of concerns
    @PostMapping("/file/{fileId}")
    public ResponseEntity<ChunkMetadata> uploadChunk(
            @PathVariable Long fileId,
            @RequestBody ChunkMetadata chunk) {
        chunk.setFileId(fileId);
        ChunkMetadata saved = service.save(chunk);
        return ResponseEntity.ok(saved);
    }

    // TODO: replace ChunkMetadata with a DTO for better separation of concerns
    @GetMapping("/{id}")
    public ResponseEntity<ChunkMetadata> getChunk(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String status) {
        service.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
