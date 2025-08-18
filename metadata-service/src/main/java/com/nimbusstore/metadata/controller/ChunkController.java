package com.nimbusstore.metadata.controller;

import com.nimbusstore.dto.ChunkMetadataDTO;
import com.nimbusstore.dto.StorageStatus;
import com.nimbusstore.metadata.model.ChunkMetadata;
import com.nimbusstore.metadata.service.ChunkService;
import com.nimbusstore.metadata.repository.ChunkRepository;
import com.nimbusstore.metadata.service.NodeCleanupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/chunks")
public class ChunkController {

    private final ChunkService chunkService;
    private final ChunkRepository chunkRepo;
    private final NodeCleanupService nodeCleanupService;

    public ChunkController(ChunkService chunkService,
                          ChunkRepository chunkRepo,
                          NodeCleanupService nodeCleanupService) {
        this.chunkService = chunkService;
        this.chunkRepo = chunkRepo;
        this.nodeCleanupService = nodeCleanupService;
    }

    /**
     * POST endpoint to upload a chunk.
     * Returns a list of all replicas for the given chunk.
     */
    @PostMapping
    public ResponseEntity<List<ChunkMetadataDTO>> uploadChunk(@RequestBody ChunkMetadataDTO chunkDto) {
        List<ChunkMetadataDTO> resultDtos = chunkService.replicateChunk(chunkDto);
        if (resultDtos == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(resultDtos);
    }

    @GetMapping("/{chunkChecksum}")
    public ResponseEntity<ChunkMetadataDTO> getChunk(@PathVariable String chunkChecksum) {
        List<ChunkMetadata> replicas = chunkRepo.findByChecksum(chunkChecksum);
        if (replicas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        ChunkMetadata liveReplica = nodeCleanupService.findLiveReplica(replicas);
        if (liveReplica != null) {
            return ResponseEntity.ok(chunkService.toDtoWithNodeUrl(liveReplica));
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam("status") StorageStatus status) {
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            chunkService.updateStatus(id, status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }
}
