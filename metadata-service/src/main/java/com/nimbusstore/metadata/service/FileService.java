package com.nimbusstore.metadata.service;

import com.nimbusstore.metadata.model.FileMetadata;
import com.nimbusstore.metadata.repository.FileRepository;
import com.nimbusstore.dto.StorageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.nimbusstore.utils.ChecksumUtils;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository repo;

    public FileService(FileRepository repo) {
        this.repo = repo;
    }

    public FileMetadata storeFile(MultipartFile file, int chunkCount) throws IOException {
        String checksum = ChecksumUtils.computeChecksum(file.getBytes());

        FileMetadata metadata = new FileMetadata(
            file.getOriginalFilename(),
            file.getSize(),
            chunkCount,
            checksum
        );
        return repo.save(metadata);
    }

    public FileMetadata retrieveFile(UUID id) {
        return repo.findById(id).orElseThrow();
    }

    public void updateStatus(UUID id, StorageStatus status) {
        FileMetadata file = repo.findById(id).orElseThrow();
        file.setStatus(status);
        repo.save(file);
    }

    public Page<FileMetadata> getAllFiles(Pageable pageable) {
        return repo.findAll(pageable);
    }
}
