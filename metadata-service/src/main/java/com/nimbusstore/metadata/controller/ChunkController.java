package com.nimbusstore.metadata.controller;

import com.nimbusstore.dto.ChunkMetadataDTO;
import com.nimbusstore.dto.StorageStatus;
import com.nimbusstore.metadata.model.ChunkMetadata;
import com.nimbusstore.metadata.service.ChunkService;
import com.nimbusstore.metadata.mapper.ChunkMetadataMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/chunks")
public class ChunkController {

    private final ChunkService service;
    private final ChunkMetadataMapper mapper;

    public ChunkController(ChunkService service, ChunkMetadataMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping("/file/{fileId}")
    public ResponseEntity<ChunkMetadataDTO> uploadChunk(
            @PathVariable UUID fileId,
            @RequestBody ChunkMetadataDTO chunkDto) {
        ChunkMetadata chunk = mapper.toEntity(chunkDto);
        chunk.setFileId(fileId);
        ChunkMetadata saved = service.save(chunk);
        ChunkMetadataDTO savedDto = mapper.toDto(saved);
        return ResponseEntity.ok(savedDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChunkMetadataDTO> getChunk(@PathVariable UUID id) {
        return service.findById(id)
                .map(chunk -> ResponseEntity.ok(mapper.toDto(chunk)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam("status") StorageStatus status) {
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        service.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
