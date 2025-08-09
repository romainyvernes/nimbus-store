package com.nimbusstore.metadata.mapper;

import com.nimbusstore.metadata.model.ChunkMetadata;
import com.nimbusstore.dto.ChunkMetadataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ChunkMetadataMapper {
    ChunkMetadataMapper INSTANCE = Mappers.getMapper(ChunkMetadataMapper.class);

    ChunkMetadataDTO toDto(ChunkMetadata entity);
    ChunkMetadata toEntity(ChunkMetadataDTO dto);
}

