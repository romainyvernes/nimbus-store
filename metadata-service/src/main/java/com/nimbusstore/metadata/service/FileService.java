package com.nimbusstore.metadata.service;

import com.nimbusstore.metadata.model.FileMetadata;
import com.nimbusstore.metadata.repository.FileRepository;
import com.nimbusstore.dto.StorageStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository repo;
    private static final int REPLICATION_FACTOR = 3;

    public FileService(FileRepository repo) {
        this.repo = repo;
    }

    public FileMetadata storeFile(MultipartFile file, int chunkCount) throws IOException {
        String checksum = computeChecksum(file.getBytes());

        FileMetadata metadata = new FileMetadata(
            file.getOriginalFilename(),
            file.getSize(),
            chunkCount,
            REPLICATION_FACTOR,
            checksum
        );
        return repo.save(metadata);
    }

    private String computeChecksum(byte[] fileBytes) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new IOException("Could not compute checksum", e);
        }
    }

    public FileMetadata retrieveFile(UUID id) {
        return repo.findById(id).orElseThrow();
    }

    public FileMetadata updateStatus(UUID id, StorageStatus status) {
        FileMetadata file = repo.findById(id).orElseThrow();
        file.setStatus(status);
        return repo.save(file);
    }
}
