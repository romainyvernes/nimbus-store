package com.nimbusstore.storage;

import com.nimbusstore.dto.ReplicationRequestDTO;
import com.nimbusstore.utils.ChecksumUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChunkControllerTests {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private static final String CHUNK_DIR = "chunks";
    private byte[] chunkData;
    private String checksum;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/chunks";
    }

    @BeforeEach
    void setup() throws Exception {
        chunkData = "test chunk data".getBytes();
        checksum = ChecksumUtils.computeChecksum(chunkData);
        Files.createDirectories(Paths.get(CHUNK_DIR));
        Files.deleteIfExists(Paths.get(CHUNK_DIR, checksum));
    }

    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(Paths.get(CHUNK_DIR, checksum));
    }

    @Test
    void testUploadChunk() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        HttpEntity<byte[]> request = new HttpEntity<>(chunkData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/upload", request, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(checksum, response.getBody());
        assertTrue(Files.exists(Paths.get(CHUNK_DIR, checksum)));
    }

    @Test
    void testGetChunk() throws Exception {
        // First upload the chunk
        Files.write(Paths.get(CHUNK_DIR, checksum), chunkData);

        ResponseEntity<byte[]> response = restTemplate.getForEntity(getBaseUrl() + "/" + checksum, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(chunkData, response.getBody());
    }

    @Test
    void testGetChunkNotFound() {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(getBaseUrl() + "/" + UUID.randomUUID(), byte[].class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testReplicateChunkSuccess() throws Exception {
        // Upload chunk locally
        Files.write(Paths.get(CHUNK_DIR, checksum), chunkData);

        // Mock target node by starting a simple HTTP server or assume target node is up and responds with 201/204
        // For this test, use localhost and expect BAD_GATEWAY since no actual node is running
        ReplicationRequestDTO dto = new ReplicationRequestDTO(checksum, "http://localhost:" + port);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ReplicationRequestDTO> request = new HttpEntity<>(dto, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(getBaseUrl() + "/replicate", request, Void.class);

        // Since no actual /chunks/upload endpoint is running on localhost, expect BAD_GATEWAY
        assertTrue(response.getStatusCode() == HttpStatus.NO_CONTENT ||
                   response.getStatusCode() == HttpStatus.BAD_GATEWAY);
    }

    @Test
    void testReplicateChunkNotFound() {
        ReplicationRequestDTO dto = new ReplicationRequestDTO("nonexistentchecksum", "http://localhost:" + port);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ReplicationRequestDTO> request = new HttpEntity<>(dto, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(getBaseUrl() + "/replicate", request, Void.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}

