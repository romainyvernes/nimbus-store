package com.nimbusstore.metadata.controller;

import com.nimbusstore.dto.FileMetadataDTO;
import com.nimbusstore.metadata.model.FileMetadata;
import com.nimbusstore.metadata.service.FileService;
import com.nimbusstore.metadata.mapper.FileMetadataMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService service;
    private final FileMetadataMapper mapper;

    public FileController(FileService service, FileMetadataMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping("/register")
    public ResponseEntity<FileMetadataDTO> registerFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chunkCount") int chunkCount) throws IOException {
        FileMetadata saved = service.storeFile(file, chunkCount);
        FileMetadataDTO dto = mapper.toDto(saved);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileMetadataDTO> retrieveFile(@PathVariable Long id) {
        FileMetadata metadata = service.retrieveFile(id);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        FileMetadataDTO dto = mapper.toDto(metadata);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String status) {
        service.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
