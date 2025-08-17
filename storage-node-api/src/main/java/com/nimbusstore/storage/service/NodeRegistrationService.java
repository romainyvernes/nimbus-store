package com.nimbusstore.storage.service;

import com.nimbusstore.dto.NodeRegistrationRequestDTO;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.UUID;

@Service
public class NodeRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(NodeRegistrationService.class);

    @Value("${metadata-service.base-url}")
    private String metadataServiceBaseUrl;

    @Value("${node.application-url}")
    private String applicationUrl;

    private static final String NODE_ID_FILE = "node-id.txt";
    private final RestTemplate restTemplate = new RestTemplate();
    @Getter
    private UUID nodeId;

    public NodeRegistrationService() {
        this.nodeId = loadOrCreateNodeId();
    }

    private UUID loadOrCreateNodeId() {
        Path path = Paths.get(NODE_ID_FILE);
        try {
            if (Files.exists(path)) {
                String idStr = Files.readString(path).trim();
                return UUID.fromString(idStr);
            } else {
                UUID newId = UUID.randomUUID();
                Files.writeString(path, newId.toString());
                return newId;
            }
        } catch (IOException e) {
            log.error("Failed to load or create nodeId file", e);
            throw new RuntimeException("Could not initialize nodeId", e);
        }
    }

    public void register() { // register with metadata service
        String url = metadataServiceBaseUrl + "/nodes/register";
        try {
            restTemplate.postForEntity(url, new NodeRegistrationRequestDTO(nodeId, applicationUrl), Void.class);
        } catch (Exception e) {
            log.error("Failed to register node with metadata service", e);
        }
    }
}
