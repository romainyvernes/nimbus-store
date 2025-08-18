package com.nimbusstore;

import com.nimbusstore.dto.ChunkMetadataDTO;
import com.nimbusstore.dto.StorageStatus;
import com.nimbusstore.metadata.MetadataApplication;
import com.nimbusstore.metadata.model.ChunkMetadata;
import com.nimbusstore.metadata.model.StorageNode;
import com.nimbusstore.metadata.repository.ChunkRepository;
import com.nimbusstore.metadata.repository.StorageNodeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    classes = MetadataApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.config.location=classpath:application-test.properties"
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChunkControllerTests {

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private StorageNodeRepository storageNodeRepository;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private UUID fileId;
    private String checksum;
    private UUID chunkId;
    private List<StorageNode> testNodes;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/chunks";
    }

    @BeforeEach
    void setup() {
        // Add storage nodes to DB for round-robin assignment
        testNodes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            StorageNode node = new StorageNode(UUID.randomUUID(),
                    System.currentTimeMillis(),"http" +
                    "://localhost:900" + i);
            storageNodeRepository.save(node);
            testNodes.add(node);
        }

        fileId = UUID.randomUUID();
        checksum = UUID.randomUUID().toString();
        ChunkMetadata chunk = new ChunkMetadata(
            0,
            testNodes.get(0).getId(),
            checksum,
            fileId
        );
        chunkRepository.save(chunk);
        chunkId = chunk.getId();
    }

    @AfterEach
    void cleanup() {
        chunkRepository.deleteAll();
        storageNodeRepository.deleteAll();
    }

    @Test
    void testUploadChunk() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("checksum", UUID.randomUUID().toString());
        payload.put("chunkIndex", 1);
        payload.put("fileId", fileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<ChunkMetadataDTO[]> response = restTemplate.postForEntity(getBaseUrl(), request, ChunkMetadataDTO[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    void testGetChunk() {
        ResponseEntity<ChunkMetadataDTO> response = restTemplate.getForEntity(getBaseUrl() + "/" + checksum, ChunkMetadataDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(checksum, response.getBody().getChecksum());
    }

    @Test
    void testGetChunkNotFound() {
        ResponseEntity<ChunkMetadataDTO> response = restTemplate.getForEntity(getBaseUrl() + "/notfoundchecksum", ChunkMetadataDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateStatus() {
        String url =
                getBaseUrl() + "/" + chunkId + "/status?status=" + StorageStatus.COMPLETED;
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PATCH, null, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ChunkMetadata updated = chunkRepository.findById(chunkId).orElse(null);
        assertNotNull(updated);
        assertEquals(StorageStatus.COMPLETED, updated.getStatus());
    }

    @Test
    void testUpdateStatusBadRequest() {
        String url = getBaseUrl() + "/" + chunkId + "/status";
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PATCH, null, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
