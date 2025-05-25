package com.unicolombo.bienestar.dto.mappers;

import com.unicolombo.bienestar.dto.request.instructor.InstructorDetailDto;
import com.unicolombo.bienestar.dto.request.instructor.InstructorListDto;
import com.unicolombo.bienestar.dto.request.instructor.InstructorPerfilDto;
import com.unicolombo.bienestar.models.Instructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InstructorMapper {

    @Mapping(target = "nombre", source = "usuario.nombre")
    @Mapping(target = "apellido", source = "usuario.apellido")
    @Mapping(target = "email", source = "usuario.email")
    InstructorDetailDto toDetailDto(Instructor instructor);

    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "nombreCompleto", expression = "java(instructor.getNombreCompleto())")
    InstructorListDto toListDto(Instructor instructor);

    @Mapping(target = "nombre", source = "usuario.nombre")
    @Mapping(target = "apellido", source = "usuario.apellido")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "fechaContratacion", expression = "java(instructor.getFechaContratacion().toString())")
    InstructorPerfilDto toPerfilDto(Instructor instructor);

    List<InstructorListDto> toListDtoList(List<Instructor> instructores);
}

