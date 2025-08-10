package com.nimbusstore.metadata.service;

import com.nimbusstore.metadata.model.FileMetadata;
import com.nimbusstore.metadata.repository.FileRepository;
import com.nimbusstore.dto.StorageStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileService {

    private final FileRepository repo;

    @Value("${nimbus.storage.location}")
    private String storageLocation;

    private static final int REPLICATION_FACTOR = 3;

    public FileService(FileRepository repo) {
        this.repo = repo;
    }

    public FileMetadata storeFile(MultipartFile file, int chunkCount) throws IOException {
        File dir = new File(storageLocation);
        if (!dir.exists()) dir.mkdirs();

        File dest = new File(dir, file.getOriginalFilename());
        file.transferTo(dest);

        // Compute checksum (SHA-256)
        String checksum = computeChecksum(dest);

        FileMetadata metadata = new FileMetadata(
            file.getOriginalFilename(),
            dest.getAbsolutePath(),
            file.getSize(),
            chunkCount,
            REPLICATION_FACTOR,
            checksum
        );
        return repo.save(metadata);
    }

    private String computeChecksum(File file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
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

    public FileMetadata retrieveFile(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public FileMetadata updateStatus(Long id, StorageStatus status) {
        FileMetadata file = repo.findById(id).orElseThrow();
        file.setStatus(status);
        return repo.save(file);
    }
}
