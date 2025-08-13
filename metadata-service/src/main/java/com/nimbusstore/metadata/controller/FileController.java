package com.nimbusstore.metadata.controller;

import com.nimbusstore.dto.FileMetadataDTO;
import com.nimbusstore.dto.StorageStatus;
import com.nimbusstore.metadata.model.FileMetadata;
import com.nimbusstore.metadata.service.FileService;
import com.nimbusstore.metadata.mapper.FileMetadataMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService service;
    private final FileMetadataMapper mapper;
    private static final int MAX_PAGE_SIZE = 100;

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
    public ResponseEntity<FileMetadataDTO> retrieveFile(@PathVariable UUID id) {
        FileMetadata metadata = service.retrieveFile(id);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        FileMetadataDTO dto = mapper.toDto(metadata);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam("status") StorageStatus status) {
        service.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int pageSize = Math.min(size, MAX_PAGE_SIZE);
        Page<FileMetadata> filePage = service.getAllFiles(PageRequest.of(page, pageSize));
        List<FileMetadataDTO> dtos = filePage.getContent().stream()
                .map(mapper::toDto)
                .toList();
        Map<String, Object> response = Map.of(
                "files", dtos,
                "total", filePage.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }
}
