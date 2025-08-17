package com.nimbusstore.storage.service;

import com.nimbusstore.dto.HeartbeatRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class HeartbeatService {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatService.class);

    @Value("${metadata-service.base-url}")
    private String metadataServiceBaseUrl;

    @Value("${heartbeat.frequency}")
    private long frequency;

    private final RestTemplate restTemplate = new RestTemplate();
    private final NodeRegistrationService nodeRegistrationService;

    public HeartbeatService(NodeRegistrationService nodeRegistrationService) {
        this.nodeRegistrationService = nodeRegistrationService;
        this.nodeRegistrationService.register();
    }

    @Scheduled(fixedRateString = "${heartbeat.frequency}")
    public void sendHeartbeat() {
        String url = metadataServiceBaseUrl + "/nodes/heartbeat";
        try {
            restTemplate.postForEntity(
                url,
                createHeartbeatPayload(),
                Void.class
            );
        } catch (Exception e) {
            log.error("Failed to send heartbeat to metadata service", e);
        }
    }

    private HeartbeatRequestDTO createHeartbeatPayload() {
        return new HeartbeatRequestDTO(nodeRegistrationService.getNodeId());
    }
}
