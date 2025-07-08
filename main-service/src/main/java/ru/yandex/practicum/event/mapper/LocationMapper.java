package ru.yandex.practicum.event.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.event.dto.LocationDto;
import ru.yandex.practicum.event.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toDto(Location location);
    Location toEntity(LocationDto dto);
}
