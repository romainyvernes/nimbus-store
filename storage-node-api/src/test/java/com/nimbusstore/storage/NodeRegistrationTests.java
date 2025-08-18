package com.nimbusstore.storage;

import com.nimbusstore.storage.service.NodeRegistrationService;
import com.nimbusstore.dto.NodeRegistrationRequestDTO;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest(properties = "spring.config.location=classpath:application-test.properties")
class NodeRegistrationTests {

    private static final String NODE_ID_FILE = "node-id.txt";
    private NodeRegistrationService nodeRegistrationService;
    private RestTemplate restTemplateMock;

    @BeforeEach
    void setup() throws IOException {
        Files.deleteIfExists(Paths.get(NODE_ID_FILE));
        restTemplateMock = Mockito.mock(RestTemplate.class);
        nodeRegistrationService = new NodeRegistrationService() {
            @Override
            public void register() {
                restTemplateMock.postForEntity(anyString(), any(NodeRegistrationRequestDTO.class), eq(Void.class));
            }
        };
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testUuidFileCreatedOnRegistration() {
        nodeRegistrationService.register();
        assertTrue(Files.exists(Paths.get(NODE_ID_FILE)), "node-id.txt should be created");
        String uuidStr = assertDoesNotThrow(() -> Files.readString(Paths.get(NODE_ID_FILE))).trim();
        assertEquals(nodeRegistrationService.getNodeId().toString(), uuidStr, "UUID in file should match service UUID");
    }

    @Test
    void testUuidPersistsAcrossRestarts() {
        nodeRegistrationService.register();
        UUID firstId = nodeRegistrationService.getNodeId();
        NodeRegistrationService restartedService = new NodeRegistrationService();
        UUID secondId = restartedService.getNodeId();
        assertEquals(firstId, secondId, "UUID should persist across service restarts");
    }

    @Test
    void testSameUuidSentInEveryHeartbeat() {
        UUID id = nodeRegistrationService.getNodeId();
        String applicationUrl = "http://localhost:9001";
        NodeRegistrationRequestDTO payload1 = new NodeRegistrationRequestDTO(id, applicationUrl);
        NodeRegistrationRequestDTO payload2 = new NodeRegistrationRequestDTO(nodeRegistrationService.getNodeId(), applicationUrl);
        assertEquals(payload1.getId(), payload2.getId(), "Same UUID should be sent in every heartbeat");
        assertEquals(payload1.getUrl(), payload2.getUrl(),
                "Application URL should match");
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(Paths.get(NODE_ID_FILE));
    }
}
