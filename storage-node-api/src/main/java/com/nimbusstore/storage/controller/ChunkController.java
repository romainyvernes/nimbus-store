package com.nimbusstore.storage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/chunks")
public class ChunkController {

    private static final String CHUNK_DIR = "chunks";

    public ChunkController() throws IOException {
        Path dirPath = Paths.get(CHUNK_DIR);
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Void> uploadChunk(@RequestParam("id") UUID chunkId,
                                            @RequestBody byte[] chunkData) {
        try {
            Path chunkPath = Paths.get(CHUNK_DIR, chunkId.toString());
            Files.write(chunkPath, chunkData);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{chunkId}")
    public ResponseEntity<byte[]> getChunk(@PathVariable UUID chunkId) {
        Path chunkPath = Paths.get(CHUNK_DIR, chunkId.toString());
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
}