package es.uah.huertojpa.depositoAgua.aplicacion.mapper;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import es.uah.huertojpa.depositoAgua.dominio.DepositoAgua;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepositoaguaMapper {
    DepositoaguaMapper mapper= Mappers.getMapper(DepositoaguaMapper.class);
    DepositoAgua toEntity(DepositoaguaDto depositoaguaDto);

    DepositoaguaDto toDto(DepositoAgua depositoagua);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    DepositoAgua partialUpdate(DepositoaguaDto depositoaguaDto, @MappingTarget DepositoAgua depositoagua);
}