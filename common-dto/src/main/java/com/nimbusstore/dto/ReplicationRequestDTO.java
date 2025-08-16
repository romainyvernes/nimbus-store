package com.nimbusstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplicationRequestDTO {
    private String checksum;
    private String targetUrl;
}

