package com.nimbusstore.storage.controller;

import com.nimbusstore.utils.ChecksumUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
}