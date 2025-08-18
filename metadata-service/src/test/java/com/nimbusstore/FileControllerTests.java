package com.nimbusstore;

import com.nimbusstore.dto.FileMetadataDTO;
import com.nimbusstore.dto.StorageStatus;
import com.nimbusstore.metadata.MetadataApplication;
import com.nimbusstore.metadata.model.FileMetadata;
import com.nimbusstore.metadata.repository.FileRepository;
import com.nimbusstore.utils.ChecksumUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    classes = MetadataApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.config.location=classpath:application-test.properties"
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileControllerTests {

    @Autowired
    private FileRepository fileRepository;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private UUID fileId;
    private String filename = "test.txt";
    private byte[] fileContent = "Hello Nimbus!".getBytes();
    private String checksum;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/files";
    }

    @BeforeEach
    void setup() {
        checksum = ChecksumUtils.computeChecksum(fileContent);
        FileMetadata file = new FileMetadata(
            filename,
            (long) fileContent.length,
            1,
            checksum
        );
        file.setStatus(StorageStatus.PENDING);
        fileRepository.save(file);
        fileId = file.getId();
    }

    @AfterEach
    void cleanup() {
        fileRepository.deleteAll();
    }

    @Test
    void testRegisterFile() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource fileResource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("chunkCount", 1);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
            new HttpEntity<>(body, headers);

        ResponseEntity<FileMetadataDTO> response = restTemplate.postForEntity(
            getBaseUrl() + "/register", requestEntity, FileMetadataDTO.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(checksum, response.getBody().getChecksum());
    }

    @Test
    void testRetrieveFile() {
        ResponseEntity<FileMetadataDTO> response = restTemplate.getForEntity(
            getBaseUrl() + "/" + fileId, FileMetadataDTO.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(checksum, response.getBody().getChecksum());
    }

    @Test
    void testRetrieveFileNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            getBaseUrl() + "/" + UUID.randomUUID(), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateStatus() {
        String url = getBaseUrl() + "/" + fileId + "/status?status=" + StorageStatus.COMPLETED;
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PATCH, null, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        FileMetadata updated = fileRepository.findById(fileId).orElse(null);
        assertNotNull(updated);
        assertEquals(StorageStatus.COMPLETED, updated.getStatus());
    }

    @Test
    void testUpdateStatusBadRequest() {
        String url = getBaseUrl() + "/" + fileId + "/status";
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PATCH, null, Void.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetAllFiles() {
        ResponseEntity<Map> response = restTemplate.getForEntity(getBaseUrl(), Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("files"));
        assertTrue(response.getBody().containsKey("total"));

        // Verify the returned list contains the expected file(s) and their properties match
        Object filesObj = response.getBody().get("files");
        assertNotNull(filesObj);
        assertInstanceOf(List.class, filesObj);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> files = (List<Map<String, Object>>) filesObj;
        assertFalse(files.isEmpty());

        Map<String, Object> file = files.getFirst();
        assertEquals(filename, file.get("filename"));
        assertEquals(checksum, file.get("checksum"));
        assertEquals(1, file.get("chunkCount"));
    }
}
