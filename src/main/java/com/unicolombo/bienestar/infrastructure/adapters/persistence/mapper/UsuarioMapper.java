package com.unicolombo.bienestar.infrastructure.adapters.persistence.mapper;

import com.unicolombo.bienestar.domain.model.Usuario;
import com.unicolombo.bienestar.infrastructure.adapters.persistence.entity.UsuarioEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    Usuario toDomain(UsuarioEntity entity);
    UsuarioEntity toEntity(Usuario domain);
}