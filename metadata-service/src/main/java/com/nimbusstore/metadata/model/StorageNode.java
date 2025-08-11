package com.nimbusstore.metadata.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorageNode {
    @Id
    @GeneratedValue
    private UUID id;
}
