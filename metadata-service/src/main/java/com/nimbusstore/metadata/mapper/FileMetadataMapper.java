package com.nimbusstore.metadata.mapper;

import com.nimbusstore.metadata.model.FileMetadata;
import com.nimbusstore.dto.FileMetadataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FileMetadataMapper {
    FileMetadataMapper INSTANCE = Mappers.getMapper(FileMetadataMapper.class);

    FileMetadataDTO toDto(FileMetadata entity);
    FileMetadata toEntity(FileMetadataDTO dto);
}

