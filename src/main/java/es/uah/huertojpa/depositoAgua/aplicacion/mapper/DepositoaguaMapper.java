package es.uah.huertojpa.depositoAgua.aplicacion.mapper;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import es.uah.huertojpa.depositoAgua.dominio.Depositoagua;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepositoaguaMapper {
    DepositoaguaMapper mapper= Mappers.getMapper(DepositoaguaMapper.class);
    Depositoagua toEntity(DepositoaguaDto depositoaguaDto);

    DepositoaguaDto toDto(Depositoagua depositoagua);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Depositoagua partialUpdate(DepositoaguaDto depositoaguaDto, @MappingTarget Depositoagua depositoagua);
}