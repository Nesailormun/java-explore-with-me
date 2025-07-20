package ru.yandex.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.request.dto.ParticipationRequestDto;
import ru.yandex.practicum.request.model.ParticipationRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {

    @Mapping(source = "event.id", target = "event")
    @Mapping(source = "requester.id", target = "requester")
    ParticipationRequestDto toDto(ParticipationRequest request);

    default List<ParticipationRequestDto> toDtoList(List<ParticipationRequest> requests) {
        return requests == null || requests.isEmpty()
                ? Collections.emptyList()
                : requests.stream().map(this::toDto).collect(Collectors.toList());
    }
}

