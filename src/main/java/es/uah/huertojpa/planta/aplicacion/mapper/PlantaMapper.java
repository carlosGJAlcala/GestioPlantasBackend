package es.uah.huertojpa.planta.aplicacion.mapper;

import es.uah.huertojpa.planta.aplicacion.dto.PlantaDto;
import es.uah.huertojpa.planta.dominio.entities.Planta;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlantaMapper {
    PlantaMapper mapper= Mappers.getMapper(PlantaMapper.class);

    Planta toEntity(PlantaDto plantaDto);

    PlantaDto toDto(Planta planta);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Planta partialUpdate(PlantaDto plantaDto, @MappingTarget Planta planta);
}