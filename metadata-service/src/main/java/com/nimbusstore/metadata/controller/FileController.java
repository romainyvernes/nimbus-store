package com.nimbusstore.metadata.controller;

import com.nimbusstore.metadata.model.FileMetadata;
import com.nimbusstore.metadata.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService service;

    public FileController(FileService service) {
        this.service = service;
    }

    // TODO: replace FileMetadata with a DTO for better separation of concerns
    @PostMapping
    public ResponseEntity<FileMetadata> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chunkCount") int chunkCount) throws IOException {
        FileMetadata saved = service.storeFile(file, chunkCount);
        return ResponseEntity.ok(saved);
    }

    // TODO: replace FileMetadata with a DTO for better separation of concerns
    @GetMapping("/{id}")
    public ResponseEntity<FileMetadata> getFileById(@PathVariable Long id) {
        FileMetadata metadata = service.retrieveFile(id);
        return ResponseEntity.ok(metadata);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String status) {
        service.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
