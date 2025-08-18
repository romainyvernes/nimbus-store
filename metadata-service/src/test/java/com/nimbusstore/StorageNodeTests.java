package com.nimbusstore;

import com.nimbusstore.dto.HeartbeatRequestDTO;
import com.nimbusstore.dto.NodeRegistrationRequestDTO;
import com.nimbusstore.metadata.MetadataApplication;
import com.nimbusstore.metadata.model.StorageNode;
import com.nimbusstore.metadata.repository.StorageNodeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    classes = MetadataApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.config.location=classpath:application-test.properties"
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StorageNodeTests {

    @Autowired
    private StorageNodeRepository storageNodeRepository;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private UUID testNodeId;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/nodes";
    }

    @Test
    @Order(1)
    void testNodeRegistrationIsRecorded() {
        testNodeId = UUID.randomUUID();
        NodeRegistrationRequestDTO registrationDTO = new NodeRegistrationRequestDTO();
        registrationDTO.setId(testNodeId);

        ResponseEntity<Void> response = restTemplate.postForEntity(getBaseUrl() + "/register", registrationDTO, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        StorageNode node = storageNodeRepository.findById(testNodeId).orElse(null);
        assertNotNull(node, "Node should be recorded in metadata service");
        assertEquals(testNodeId, node.getId());
    }

    @Test
    @Order(2)
    void testHeartbeatUpdatesLastHeartbeat() {
        testNodeId = UUID.randomUUID();
        // Register node first
        NodeRegistrationRequestDTO registrationDTO = new NodeRegistrationRequestDTO();
        registrationDTO.setId(testNodeId);
        restTemplate.postForEntity(getBaseUrl() + "/register", registrationDTO, Void.class);

        StorageNode node = storageNodeRepository.findById(testNodeId).orElse(null);
        assertNotNull(node);

        long before = node.getLastHeartbeat();

        HeartbeatRequestDTO heartbeatDTO = new HeartbeatRequestDTO();
        heartbeatDTO.setId(testNodeId);

        ResponseEntity<Void> response = restTemplate.postForEntity(getBaseUrl() + "/heartbeat", heartbeatDTO, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        StorageNode updatedNode = storageNodeRepository.findById(testNodeId).orElse(null);
        assertNotNull(updatedNode);
        assertTrue(updatedNode.getLastHeartbeat() >= before, "lastHeartbeat should be updated");
    }
}
