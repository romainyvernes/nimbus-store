package com.nimbusstore.metadata.repository;

import com.nimbusstore.metadata.model.FailedReplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FailedReplicationRepository extends JpaRepository<FailedReplication, UUID> {
}