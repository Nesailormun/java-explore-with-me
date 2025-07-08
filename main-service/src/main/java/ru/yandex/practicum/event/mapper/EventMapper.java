package ru.yandex.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.yandex.practicum.category.mapper.CategoryMapper;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.NewEventDto;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class, LocationMapper.class})
public interface EventMapper {

    @Mapping(source = "initiator", target = "initiator")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "state", target = "state")
    @Mapping(target = "views", ignore = true)
    default EventFullDto toFullDto(Event event) {
        return null;
    }

    @Mapping(source = "initiator", target = "initiator")
    @Mapping(source = "category", target = "category")
    @Mapping(target = "views", ignore = true)
    EventShortDto toShortDto(Event event);

    @Mapping(source = "category", target = "category", qualifiedByName = "mapCategoryId")
    @Mapping(source = "location", target = "location")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "state", constant = "PENDING")
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "id", ignore = true)
    Event fromDto(NewEventDto dto);

    @Named("mapCategoryId")
    default Category mapCategoryId(Long id) {
        Category category = new Category();
        category.setId(id);
        return category;
    }
}
