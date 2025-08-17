package com.nimbusstore.storage.controller;

import com.nimbusstore.utils.ChecksumUtils;
import com.nimbusstore.dto.ReplicationRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/chunks")
public class ChunkController {

    private static final Logger logger = LoggerFactory.getLogger(ChunkController.class);
    private static final String CHUNK_DIR = "chunks";

    public ChunkController() throws IOException {
        Path dirPath = Paths.get(CHUNK_DIR);
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadChunk(@RequestBody byte[] chunkData) {
        try {
            String checksum = ChecksumUtils.computeChecksum(chunkData);
            Path chunkPath = Paths.get(CHUNK_DIR, checksum);
            Files.write(chunkPath, chunkData);
            return ResponseEntity.status(HttpStatus.CREATED).body(checksum);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{chunkChecksum}")
    public ResponseEntity<byte[]> getChunk(@PathVariable String chunkChecksum) {
        Path chunkPath = Paths.get(CHUNK_DIR, chunkChecksum);
        if (Files.exists(chunkPath)) {
            try {
                byte[] chunk = Files.readAllBytes(chunkPath);
                return ResponseEntity.ok(chunk);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/replicate")
    public ResponseEntity<Void> replicateChunk(@RequestBody ReplicationRequestDTO request) {
        Path chunkPath = Paths.get(CHUNK_DIR, request.getChecksum());
        if (!Files.exists(chunkPath)) {
            logger.error("Chunk with checksum {} not found.", request.getChecksum());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            byte[] chunkData = Files.readAllBytes(chunkPath);
            String uploadUrl = request.getTargetUrl() + "/chunks/upload";
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, chunkData, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                logger.error("Failed to replicate chunk to target node {}. Status: {}", request.getTargetUrl(), response.getStatusCode());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }
        } catch (IOException e) {
            logger.error("Error reading chunk data for checksum {}: {}", request.getChecksum(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Error replicating chunk to target node {}: {}", request.getTargetUrl(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}