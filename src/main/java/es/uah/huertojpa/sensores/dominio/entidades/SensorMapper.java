package es.uah.huertojpa.sensores.dominio.entidades;

import es.uah.huertojpa.planta.aplicacion.mapper.PlantaMapper;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SensorMapper {
    SensorMapper mapper= Mappers.getMapper(SensorMapper.class);

    Sensor toEntity(SensorDto sensorDto);

    SensorDto toDto(Sensor sensor);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Sensor partialUpdate(SensorDto sensorDto, @MappingTarget Sensor sensor);
}