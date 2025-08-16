package com.nimbusstore.metadata.controller;

import com.nimbusstore.dto.ChunkMetadataDTO;
import com.nimbusstore.dto.StorageStatus;
import com.nimbusstore.metadata.model.ChunkMetadata;
import com.nimbusstore.metadata.service.ChunkService;
import com.nimbusstore.metadata.mapper.ChunkMetadataMapper;
import com.nimbusstore.metadata.repository.ChunkRepository;
import com.nimbusstore.metadata.repository.StorageNodeRepository;
import com.nimbusstore.metadata.service.NodeCleanupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/chunks")
public class ChunkController {

    private final ChunkService service;
    private final ChunkMetadataMapper mapper;
    private final ChunkRepository chunkRepo;
    private final StorageNodeRepository nodeRepo;
    private final NodeCleanupService nodeCleanupService;

    @Value("${dead-node-threshold}")
    private long deadNodeThreshold;

    public ChunkController(ChunkService service, ChunkMetadataMapper mapper,
                          ChunkRepository chunkRepo, StorageNodeRepository nodeRepo,
                          NodeCleanupService nodeCleanupService) {
        this.service = service;
        this.mapper = mapper;
        this.chunkRepo = chunkRepo;
        this.nodeRepo = nodeRepo;
        this.nodeCleanupService = nodeCleanupService;
    }

    @PostMapping
    public ResponseEntity<List<ChunkMetadataDTO>> uploadChunk(@RequestBody ChunkMetadataDTO chunkDto) {
        List<ChunkMetadataDTO> resultDtos = service.replicateChunk(chunkDto);
        return ResponseEntity.ok(resultDtos);
    }

    @GetMapping("/{chunkChecksum}")
    public ResponseEntity<ChunkMetadataDTO> getChunk(@PathVariable String chunkChecksum) {
        List<ChunkMetadata> replicas = chunkRepo.findByChecksum(chunkChecksum);
        if (replicas.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ChunkMetadata liveReplica = nodeCleanupService.findLiveReplica(replicas);
        if (liveReplica != null) {
            return ResponseEntity.ok(service.toDtoWithNodeUrl(liveReplica));
        }
        return ResponseEntity.status(404).build();
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
