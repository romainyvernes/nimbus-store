package com.nimbusstore;

import com.nimbusstore.dto.NodeRegistrationRequestDTO;
import com.nimbusstore.dto.HeartbeatRequestDTO;
import com.nimbusstore.metadata.model.StorageNode;
import com.nimbusstore.metadata.repository.StorageNodeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MetadataServiceIntegrationTests {

    @Autowired
    private StorageNodeRepository storageNodeRepository;

    private UUID testNodeId;

    @Test
    @Order(1)
    void testNodeRegistrationIsRecorded() {
        testNodeId = UUID.randomUUID();
        storageNodeRepository.save(new StorageNode(testNodeId, System.currentTimeMillis()));
        StorageNode node = storageNodeRepository.findById(testNodeId).orElse(null);
        assertNotNull(node, "Node should be recorded in metadata service");
        assertEquals(testNodeId, node.getId());
    }

    @Test
    @Order(2)
    void testHeartbeatUpdatesLastHeartbeat() {
        testNodeId = UUID.randomUUID();
        StorageNode node = new StorageNode(testNodeId, System.currentTimeMillis());
        storageNodeRepository.save(node);

        long before = node.getLastHeartbeat();
        node.setLastHeartbeat(System.currentTimeMillis());
        storageNodeRepository.save(node);

        StorageNode updatedNode = storageNodeRepository.findById(testNodeId).orElse(null);
        assertNotNull(updatedNode);
        assertTrue(updatedNode.getLastHeartbeat() >= before, "lastHeartbeat should be updated");
    }
}

