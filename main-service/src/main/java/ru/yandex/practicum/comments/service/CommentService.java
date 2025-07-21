package ru.yandex.practicum.comments.service;

import ru.yandex.practicum.comments.dto.CommentDto;
import ru.yandex.practicum.comments.dto.CommentStatusUpdateDto;
import ru.yandex.practicum.comments.dto.NewCommentDto;
import ru.yandex.practicum.comments.dto.UpdateCommentDto;

import java.util.List;


public interface CommentService {

    CommentDto createComment(Long userId, Long eventId, NewCommentDto dto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto);

    void deleteComment(Long userId, Long commentId);

    List<CommentDto> getEventComments(Long eventId);

    List<CommentDto> getUserComments(Long userId);

    CommentDto moderateComment(Long commentId, CommentStatusUpdateDto dto);

    List<CommentDto> getCommentsForModeration();

    void deleteCommentByAdmin(Long commentId);
}
