package ru.yandex.practicum.comments.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.comments.dto.CommentDto;
import ru.yandex.practicum.comments.dto.NewCommentDto;
import ru.yandex.practicum.comments.dto.UpdateCommentDto;
import ru.yandex.practicum.comments.entity.Comment;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.user.mapper.UserMapper;
import ru.yandex.practicum.user.model.User;

import java.time.LocalDateTime;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class, EventMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {LocalDateTime.class}
)
public interface CommentMapper {

    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author", target = "authorName", qualifiedByName = "mapAuthorName")
    @Mapping(source = "createdOn", target = "createdOn")
    @Mapping(source = "updatedOn", target = "updatedOn")
    @Mapping(source = "status", target = "status")
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", source = "event")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "text", source = "dto.text")
    @Mapping(target = "createdOn", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "status", expression = "java(ru.yandex.practicum.comments.entity.CommentStatus.PENDING_MODERATION)")
    Comment toEntity(NewCommentDto dto, Event event, User author);

    @Mapping(target = "text", source = "dto.text")
    @Mapping(target = "updatedOn", expression = "java(LocalDateTime.now())")
    @Mapping(target = "status", expression = "java(ru.yandex.practicum.comments.entity.CommentStatus.PENDING_MODERATION)")
    void updateFromDto(UpdateCommentDto dto, @MappingTarget Comment comment);

    @Named("mapAuthorName")
    default String mapAuthorName(User author) {
        return author != null ? author.getName() : null;
    }
}
